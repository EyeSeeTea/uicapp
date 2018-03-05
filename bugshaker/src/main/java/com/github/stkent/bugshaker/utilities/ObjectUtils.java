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
package com.github.stkent.bugshaker.utilities;


import android.support.annotation.NonNull;

public final class ObjectUtils {

    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    public static <T> T requireNonNull(T obj, @NonNull String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }

        return obj;
    }

    public static boolean isNonNull(Object obj) {
        return obj != null;
    }

    private ObjectUtils() {
    }
}
