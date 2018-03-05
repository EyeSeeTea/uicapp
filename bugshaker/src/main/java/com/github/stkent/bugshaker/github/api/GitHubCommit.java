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


public class GitHubCommit {

    private final String message;
    private final String branch;
    private final String content;

    public GitHubCommit(String message, String branch, String content) {
        this.message = message;
        this.branch = branch;
        this.content = content;
    }

    public String getMessage() {
        return message;
    }

    public String getBranch() {
        return branch;
    }

    public String getContent() {
        return content;
    }
}
