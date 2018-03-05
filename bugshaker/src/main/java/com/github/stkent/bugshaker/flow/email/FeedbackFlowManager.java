/*
 * Copyright 2016 Stuart Kent
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
package com.github.stkent.bugshaker.flow.email;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.github.stkent.bugshaker.ActivityReferenceManager;
import com.github.stkent.bugshaker.ApplicationInfoProvider;
import com.github.stkent.bugshaker.flow.FeedbackProvider;
import com.github.stkent.bugshaker.flow.dialog.DialogProvider;
import com.github.stkent.bugshaker.flow.email.screenshot.ScreenshotProvider;
import com.github.stkent.bugshaker.utilities.ActivityUtils;
import com.github.stkent.bugshaker.utilities.Logger;
import com.github.stkent.bugshaker.utilities.Toaster;


import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public final class FeedbackFlowManager {

    private static final int FLAG_SECURE_VALUE = 0x00002000;

    @NonNull
    private final Toaster toaster;

    @NonNull
    private final ActivityReferenceManager activityReferenceManager;

    @NonNull
    private final FeedbackProvider feedbackProvider;

    @NonNull
    private final ScreenshotProvider screenshotProvider;

    @NonNull
    private final DialogProvider alertDialogProvider;

    @NonNull
    private final Logger logger;

    @NonNull
    private final ApplicationInfoProvider applicationInfoProvider;

    @Nullable
    private Dialog alertDialog;

    private boolean ignoreFlagSecure;

    private final OnClickListener reportBugClickListener = new OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            final Activity activity = activityReferenceManager.getValidatedActivity();
            if (activity == null) {
                return;
            }

            final String devicesInfo = applicationInfoProvider.getApplicationInfo();
            if (shouldAttemptToCaptureScreenshot(activity)) {
                screenshotProvider.getScreenshotUri(activity)
                        .single()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Uri>() {
                            @Override
                            public void onCompleted() {
                                // This method intentionally left blank.
                            }

                            @Override
                            public void onError(final Throwable e) {
                                final String errorString = "Screenshot capture failed";
                                toaster.toast(errorString);
                                logger.e(errorString);

                                logger.printStackTrace(e);

                                feedbackProvider.submitFeedback(activity, null, devicesInfo,
                                        logger.isLoggingEnabled());
                            }

                            @Override
                            public void onNext(final Uri uri) {
                                feedbackProvider.submitFeedback(activity, uri, devicesInfo,
                                        logger.isLoggingEnabled());
                            }
                        });

            } else {
                final String warningString = "Window is secured; no screenshot taken";

                toaster.toast(warningString);
                logger.d(warningString);
                feedbackProvider.submitFeedback(activity, null, devicesInfo,
                        logger.isLoggingEnabled());
            }
        }
    };

    public FeedbackFlowManager(
            @NonNull final Toaster toaster,
            @NonNull final ActivityReferenceManager activityReferenceManager,
            @NonNull final FeedbackProvider feedbackProvider,
            @NonNull final ScreenshotProvider screenshotProvider,
            @NonNull final DialogProvider alertDialogProvider,
            @NonNull final Logger logger,
            @NonNull final ApplicationInfoProvider applicationInfoProvider) {

        this.toaster = toaster;
        this.activityReferenceManager = activityReferenceManager;
        this.feedbackProvider = feedbackProvider;
        this.screenshotProvider = screenshotProvider;
        this.alertDialogProvider = alertDialogProvider;
        this.logger = logger;
        this.applicationInfoProvider = applicationInfoProvider;
    }

    public void onActivityResumed(@NonNull final Activity activity) {
        dismissDialog();
        activityReferenceManager.setActivity(activity);
    }

    public void onActivityStopped() {
        dismissDialog();
    }

    public void startFlowIfNeeded(final boolean ignoreFlagSecure) {

        if (isFeedbackFlowStarted()) {
            logger.d("Feedback flow already started; ignoring shake.");
            return;
        }
        this.ignoreFlagSecure = ignoreFlagSecure;

        showDialog();
    }

    private boolean isFeedbackFlowStarted() {
        return alertDialog != null && alertDialog.isShowing();
    }

    private void showDialog() {
        final Activity currentActivity = activityReferenceManager.getValidatedActivity();
        if (currentActivity == null) {
            return;
        }

        alertDialog = alertDialogProvider.getAlertDialog(currentActivity, reportBugClickListener);
        alertDialog.show();
    }

    private void dismissDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    private boolean shouldAttemptToCaptureScreenshot(@NonNull final Activity activity) {
        final int windowFlags = ActivityUtils.getWindow(activity).getAttributes().flags;

        final boolean isWindowSecured =
                (windowFlags & WindowManager.LayoutParams.FLAG_SECURE) == FLAG_SECURE_VALUE;

        final boolean result = ignoreFlagSecure || !isWindowSecured;

        if (!isWindowSecured) {
            logger.d("Window is not secured; should attempt to capture screenshot.");
        } else {
            if (ignoreFlagSecure) {
                logger.d("Window is secured, but we're ignoring that.");
            } else {
                logger.d("Window is secured, and we're respecting that.");
            }
        }

        return result;
    }

}
