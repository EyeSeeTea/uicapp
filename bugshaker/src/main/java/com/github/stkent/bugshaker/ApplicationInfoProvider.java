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
package com.github.stkent.bugshaker;


import android.support.annotation.NonNull;

import com.github.stkent.bugshaker.flow.email.App;
import com.github.stkent.bugshaker.flow.email.Device;
import com.github.stkent.bugshaker.flow.email.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ApplicationInfoProvider {


    private @NonNull final App app;
    private @NonNull final Environment environment;
    private @NonNull final Device device;

    public ApplicationInfoProvider(@NonNull App app,
            @NonNull Environment environment,
            @NonNull Device device) {
        this.app = app;
        this.environment = environment;
        this.device = device;
    }

    @NonNull
    public String getApplicationInfo() {

        final String androidVersionString = String.format(
                "%s (%s)", environment.getAndroidVersionName(), environment.getAndroidVersionCode());

        final String appVersionString = String.format("%s (%s)", app.getVersionName(), app.getVersionCode());

        // @formatter:off
        return    "Time Stamp: " + getCurrentUtcTimeStringForDate(new Date()) + "\n"
                + "App Version: " + appVersionString + "\n"
                + "Install Source: " + app.getInstallSource() + "\n"
                + "Android Version: " + androidVersionString + "\n"
                + "Device Manufacturer: " + device.getManufacturer() + "\n"
                + "Device Model: " + device.getModel() + "\n"
                + "Display Resolution: " + device.getResolution() + "\n"
                + "Display Density (Actual): " + device.getActualDensity() + "\n"
                + "Display Density (Bucket) " + device.getDensityBucket() + "\n"
                + "---------------------\n\n";
        // @formatter:on
    }

    @NonNull
    private String getCurrentUtcTimeStringForDate(final Date date) {
        final SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z", Locale.getDefault());

        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return simpleDateFormat.format(date);
    }
}
