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
package com.github.stkent.bugshaker.github.api;

import android.support.annotation.NonNull;

import com.github.stkent.bugshaker.github.BugReportProvider;
import com.github.stkent.bugshaker.github.GitHubConfiguration;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;


public class GitHubApiProvider implements BugReportProvider {

    @NonNull
    private final GitHubConfiguration gitHubConfiguration;
    private final GitHubApi gitHubApi;

    public GitHubApiProvider(
            @NonNull final GitHubConfiguration gitHubConfiguration) {
        this.gitHubConfiguration = gitHubConfiguration;

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(GitHubApi.BASE_URL)
                .build();
        this.gitHubApi = retrofit.create(GitHubApi.class);

    }

    @Override
    public Observable<GitHubResponse> addIssue(@NonNull Issue newIssue) {

        String[] repoArray = gitHubConfiguration.getRepositoryName().split("/");
        String owner = repoArray[0];
        String repo = repoArray[1];
        String token = gitHubConfiguration.getAuthenticationToken();

        return gitHubApi.addIssue(newIssue, owner, repo, "token " + token);
    }

    @Override
    public Observable<GitHubResponse> uploadScreenShot(@NonNull String fileName, @NonNull String base64File) {

        String[] repoArray = gitHubConfiguration.getScreenShotRepositoryName().split("/");
        String owner = repoArray[0];
        String repo = repoArray[1];

        String token = gitHubConfiguration.getAuthenticationToken();
        String branch = gitHubConfiguration.getScreenShotBranchName();

        GitHubCommit commit = new GitHubCommit("Android device snapshot", branch, base64File);

        return gitHubApi.uploadFile(commit, owner, repo, fileName, "token " + token);
    }
}
