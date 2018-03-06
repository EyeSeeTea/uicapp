package org.eyeseetea.uicapp;


import android.app.Application;

import com.github.stkent.bugshaker.BugShaker;
import com.github.stkent.bugshaker.flow.dialog.AlertDialogType;
import com.github.stkent.bugshaker.github.GitHubConfiguration;

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        BugShaker.get(this)
                .setLoggingEnabled(BuildConfig.DEBUG)
                .setAlertDialogType(AlertDialogType.APP_COMPAT)
                .setGitHubInfo(new GitHubConfiguration(
                        "eyeseetea/uicapp",
                        "96a31419f58357fab95ec80b56a43c9a1868b429",
                        "eyeseeteabottest/snapshots",
                        "master"))
                .assemble()
                .start();
    }
}
