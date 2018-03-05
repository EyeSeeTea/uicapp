/*
 * Copyright 2018 EyeSeeTea
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.stkent.bugshaker.github;

import static com.github.stkent.bugshaker.utilities.FileUtils.getExtensionFrom;
import static com.github.stkent.bugshaker.utilities.FileUtils.getUniqueFileName;
import static com.github.stkent.bugshaker.utilities.NetworkUtils.isDeviceConnected;
import static com.github.stkent.bugshaker.utilities.StringUtils.createMarkdownCodeBlock;
import static com.github.stkent.bugshaker.utilities.StringUtils.createMarkdownFileBlock;
import static com.github.stkent.bugshaker.utilities.StringUtils.createMarkdownTitle1;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.github.stkent.bugshaker.R;
import com.github.stkent.bugshaker.github.api.GitHubApiProvider;
import com.github.stkent.bugshaker.github.api.GitHubResponse;
import com.github.stkent.bugshaker.github.api.Issue;
import com.github.stkent.bugshaker.utilities.ImageUtils;
import com.github.stkent.bugshaker.utilities.Logger;
import com.github.stkent.bugshaker.utilities.Toaster;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ReportBugActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_GIT_HUB_CONFIGURATION = "EXTRA_GIT_HUB_CONFIGURATION";
    public static final String EXTRA_DEVICE_INFO = "EXTRA_DEVICE_INFO";
    public static final String EXTRA_IS_LOGGER_ACTIVE = "EXTRA_IS_LOGGER_ACTIVE";
    private static final int DELAY_BEFORE_SENDING_REPORT = 4;
    private static final int PROGRESS_BAR_ELEVATION = 5;

    private String deviceInfo;
    private EditText issueTitleEditText;
    private EditText bugReportEditText;
    private Toaster toaster;
    private ProgressBar progressBar;
    private BugReportProvider bugReportProvider;
    private Button reportBugButton;
    private Logger logger;
    private Uri screenShotUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_bug);
        bindUI();

        initValuesFromIntent();

    }

    @Override
    public void onClick(View view) {

        if (areFieldsNotEmpty()) {
            submitBugOnlyWithNetworkConnectivity();
        } else {
            issueTitleEditText.setError(getString(R.string.required));
        }
    }

    private void submitBugOnlyWithNetworkConnectivity() {
        if (isDeviceConnected(this)) {
            submitBug();
        } else {
            toaster.toast(R.string.error_network_connection_not_available);
        }
    }

    private void initValuesFromIntent() {
        GitHubConfiguration gitHubConfiguration = getIntent().getParcelableExtra(
                EXTRA_GIT_HUB_CONFIGURATION);

        deviceInfo = getIntent().getStringExtra(EXTRA_DEVICE_INFO);
        boolean isLoggerActive = getIntent().getBooleanExtra(EXTRA_IS_LOGGER_ACTIVE, false);
        screenShotUri = getIntent().getData();
        logger = new Logger(isLoggerActive);
        bugReportProvider = new GitHubApiProvider(gitHubConfiguration);
    }

    private void bindUI() {
        progressBar = findViewById(R.id.progressBar);
        toaster = new Toaster(getApplicationContext());
        issueTitleEditText = findViewById(R.id.issue_title);
        bugReportEditText = findViewById(R.id.bug_text);

        reportBugButton = findViewById(R.id.report_bug);

        reportBugButton.setOnClickListener(this);

    }

    private void submitBug() {

        prepareUIComponentsBeforeSubmit();

        createNewIssue()
                //It avoids the activity finishes too quickly
                // and causes bad user experience
                .delay(DELAY_BEFORE_SENDING_REPORT, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GitHubResponse>() {
                    @Override
                    public void onCompleted() {
                        prepareUIComponentsAfterSubmit();
                    }

                    @Override
                    public void onError(Throwable e) {
                        prepareUIComponentsAfterSubmit();
                        toaster.toast(R.string.error_unable_to_send_report);
                        logger.printStackTrace(e);
                    }

                    @Override
                    public void onNext(GitHubResponse gitHubResponse) {
                        logger.d("GitHub issue Created");
                        toaster.toast(R.string.bug_submitted);
                        finish();
                    }
                });
    }

    private boolean areFieldsNotEmpty() {
        return !issueTitleEditText.getText().toString().trim().isEmpty();
    }

    @NonNull
    private Issue getIssueFromUI(String screenShotServerUrl) {

        String issueTitle = issueTitleEditText.getText().toString();
        String bugReportText = bugReportEditText.getText().toString();

        return new Issue(issueTitle, getBodyIssue(screenShotServerUrl, bugReportText));
    }

    @NonNull
    private String getBodyIssue(@Nullable String screenShotServerUrl,
            @NonNull String bugReportText) {

        return createMarkdownTitle1("User Report")
                + "\n"
                + bugReportText
                + "\n"
                + getMarkdownScreenshotSection(screenShotServerUrl)
                + createMarkdownTitle1("Device Info")
                + "\n"
                + createMarkdownCodeBlock(deviceInfo);
    }

    private void prepareUIComponentsBeforeSubmit() {
        issueTitleEditText.setEnabled(false);
        reportBugButton.setEnabled(false);
        bugReportEditText.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        //Adding elevation progressBar to old versions of Android
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setTranslationZ(progressBar, PROGRESS_BAR_ELEVATION);
        }
    }

    private void prepareUIComponentsAfterSubmit() {
        issueTitleEditText.setEnabled(true);
        reportBugButton.setEnabled(true);
        bugReportEditText.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private String getScreenShotOnBase64Format() throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), screenShotUri);

        return ImageUtils.toBase64(bitmap);
    }

    private Observable<GitHubResponse> createNewIssue() {

        if (screenShotUri != null) {
            try {
                String base64Image = getScreenShotOnBase64Format();
                String fileExtension = getExtensionFrom(screenShotUri);
                String uniqueFileName = getUniqueFileName(fileExtension, logger);

                //Uploading Screenshot to GitHub
                return bugReportProvider.uploadScreenShot(uniqueFileName, base64Image)
                        .flatMap(new Func1<GitHubResponse, Observable<? extends GitHubResponse>>() {
                            @Override
                            public Observable<? extends GitHubResponse> call(GitHubResponse
                                    fileResponse) {
                                String screenShotServerUrl =
                                        fileResponse.getContent().getDownloadURL();
                                Issue newIssue = getIssueFromUI(screenShotServerUrl);

                                logger.d("screenShot uploaded url: " + screenShotServerUrl);
                                //Creating GitHub issue
                                return bugReportProvider.addIssue(newIssue);
                            }
                        });

            } catch (IOException e) {
                logger.printStackTrace(e);
                toaster.toast(R.string.error_unable_to_attach_screenshot_file);
            }
        }

        Issue newIssue = getIssueFromUI(null);
        return bugReportProvider.addIssue(newIssue);
    }

    @NonNull
    private String getMarkdownScreenshotSection(@Nullable String screenShotServerUrl) {
        String string = "";

        if (screenShotServerUrl != null) {
            string = "\n"
                    + createMarkdownTitle1("ScreenShot")
                    + "\n"
                    + createMarkdownFileBlock(screenShotServerUrl)
                    + "\n";
        }
        return string;
    }
}
