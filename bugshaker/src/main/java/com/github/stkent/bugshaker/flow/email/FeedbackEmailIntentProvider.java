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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.stkent.bugshaker.flow.FeedbackProvider;
import com.github.stkent.bugshaker.utilities.Logger;

import java.util.Arrays;
import java.util.List;

public final class FeedbackEmailIntentProvider implements FeedbackProvider {

    private static final String DEFAULT_EMAIL_SUBJECT_LINE_SUFFIX = " Android App Feedback";

    @NonNull
    private final GenericEmailIntentProvider genericEmailIntentProvider;

    @NonNull
    private final App app;

    @NonNull
    private final String[] emailAddresses;

    @NonNull
    private final String emailSubjectLine;

    @NonNull
    private final EmailCapabilitiesProvider emailCapabilitiesProvider;

    @NonNull
    private final Context applicationContext;

    @NonNull
    private final Logger logger;

    public FeedbackEmailIntentProvider(
            @NonNull final Context context,
            @NonNull final GenericEmailIntentProvider genericEmailIntentProvider,
            @NonNull String[] emailAddresses,
            @NonNull final String emailSubjectLine,
            @NonNull final EmailCapabilitiesProvider emailCapabilitiesProvider,
            @NonNull final Context applicationContext,
            @NonNull Logger logger) {

        this.genericEmailIntentProvider = genericEmailIntentProvider;
        this.app = new App(context);
        this.emailAddresses = Arrays.copyOf(emailAddresses, emailAddresses.length);
        this.emailSubjectLine = emailSubjectLine;
        this.emailCapabilitiesProvider = emailCapabilitiesProvider;
        this.applicationContext = applicationContext;
        this.logger = logger;
    }

    @NonNull
    private Intent getFeedbackEmailIntent(
            @NonNull final String[] emailAddresses,
            @Nullable final String userProvidedEmailSubjectLine,
            @NonNull final String emailBody) {

        final String emailSubjectLine = getEmailSubjectLine(userProvidedEmailSubjectLine);

        return genericEmailIntentProvider
                .getEmailIntent(emailAddresses, emailSubjectLine, emailBody);
    }

    @NonNull
    private Intent getFeedbackEmailIntent(
            @NonNull final String[] emailAddresses,
            @Nullable final String userProvidedEmailSubjectLine,
            @NonNull final Uri screenshotUri, @NonNull final String emailBody) {

        final String emailSubjectLine = getEmailSubjectLine(userProvidedEmailSubjectLine);

        return genericEmailIntentProvider
                .getEmailWithAttachmentIntent(
                        emailAddresses, emailSubjectLine, emailBody, screenshotUri);
    }

    @NonNull
    private String getEmailSubjectLine(@Nullable final String userProvidedEmailSubjectLine) {
        if (userProvidedEmailSubjectLine != null) {
            return userProvidedEmailSubjectLine;
        }

        return app.getName() + DEFAULT_EMAIL_SUBJECT_LINE_SUFFIX;
    }


    @Override
    public void submitFeedback(@NonNull Activity activity,
            @Nullable Uri screenShotUri, @NonNull String applicationInfo,
            final boolean loggingEnabled) {

        if (emailCapabilitiesProvider.canSendEmailsWithAttachments()) {
            sendEmailWithScreenshot(activity, screenShotUri, applicationInfo);
        } else {
            sendEmailWithoutScreenshot(activity, applicationInfo);
        }

    }

    private void sendEmailWithScreenshot(
            @NonNull final Activity activity,
            @NonNull final Uri screenshotUri, @NonNull final String applicationInfo) {

        final Intent feedbackEmailIntent = getFeedbackEmailIntent(
                emailAddresses,
                emailSubjectLine,
                screenshotUri, applicationInfo);

        final List<ResolveInfo> resolveInfoList = applicationContext.getPackageManager()
                .queryIntentActivities(feedbackEmailIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (final ResolveInfo receivingApplicationInfo : resolveInfoList) {
            // FIXME: revoke these permissions at some point!
            applicationContext.grantUriPermission(
                    receivingApplicationInfo.activityInfo.packageName,
                    screenshotUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        activity.startActivity(feedbackEmailIntent);

        logger.d("Sending email with screenshot.");
    }

    private void sendEmailWithoutScreenshot(@NonNull final Activity activity,
            @NonNull final String applicationInfo) {
        final Intent feedbackEmailIntent = getFeedbackEmailIntent(
                emailAddresses,
                emailSubjectLine, applicationInfo);

        activity.startActivity(feedbackEmailIntent);

        logger.d("Sending email with no screenshot.");
    }
}
