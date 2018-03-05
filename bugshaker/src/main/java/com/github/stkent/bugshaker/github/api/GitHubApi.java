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

import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public interface GitHubApi {

    String BASE_URL = "https://api.github.com";

    @POST("/repos/{owner}/{repo}/issues")
    Observable<GitHubResponse> addIssue(@Body @NonNull Issue issue, @Path("owner") String repoOwner,
            @Path("repo") String repo, @Header("Authorization") String token);

    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @PUT("/repos/{owner}/{repo}/contents/{fileName}")
    Observable<GitHubResponse> uploadFile(@Body @NonNull GitHubCommit commit,
            @NonNull @Path("owner") String repoOwner,
            @NonNull @Path("repo") String repo,
            @NonNull @Path("fileName") String fileName,
            @NonNull @Header("Authorization") String token);

}
