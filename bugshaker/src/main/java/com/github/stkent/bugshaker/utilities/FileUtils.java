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


import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

public final class FileUtils {

    @NonNull
    public static String getUniqueFileName(@NonNull final String extension, Logger logger) {
        String filename = "";

        try {
            File file = File.createTempFile("android_screenshot", extension);
            filename = file.getName();
            //noinspection ResultOfMethodCallIgnored
            if (!file.delete()) {
                logger.d("Temporal file: " + filename + " not deleted");
            }
        } catch (IOException e) {
            logger.printStackTrace(e);
        }
        return filename;
    }

    @NonNull
    public static String getExtensionFrom(@NonNull final Uri uri) {
        String uriString = uri.getPath();
        String extension = "";

        if (uriString.contains(".")) {
            extension = uriString.substring(uriString.lastIndexOf('.'));
        }
        return extension;
    }

    private FileUtils() {

    }

}
