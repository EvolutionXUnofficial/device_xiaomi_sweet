/*
 * Copyright (C) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.device;

import android.provider.Settings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.Constants;
import org.lineageos.settings.device.utils.DisplayUtils;
import org.lineageos.settings.device.utils.FileUtils;
import org.lineageos.settings.device.utils.KcalUtils;

import vendor.xiaomi.hardware.touchfeature.V1_0.ITouchFeature;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // We need to reset this setting to trigger an update in display service
        final float refreshRate = Settings.System.getFloat(context.getContentResolver(),
            Settings.System.MIN_REFRESH_RATE, 120.0f);
        Settings.System.putFloat(context.getContentResolver(),
            Settings.System.MIN_REFRESH_RATE, refreshRate);

        ContentObserver observer = new ContentObserver(Handler.getMain()) {
            @Override
            public void onChange(boolean selfChange) {
                updateTapToWakeStatus(context);
            }
        };

        context.getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.DOUBLE_TAP_TO_WAKE), true, observer);

        updateTapToWakeStatus(context);
        DisplayUtils.setDcDimmingStatus(sharedPreferences.getBoolean(Constants.KEY_DC_DIMMING, false));

        if (KcalUtils.isKcalSupported()) {
            KcalUtils.writeCurrentSettings(sharedPreferences);
        }
    }

    private void updateTapToWakeStatus(Context context) {
       try {
            ITouchFeature.getService().setTouchMode(Constants.DT2W_TOUCH_FEATURE,
                    (Settings.Secure.getInt(context.getContentResolver(),
                            Settings.Secure.DOUBLE_TAP_TO_WAKE, 0) == 1) ? 1 : 0);
       } catch (Exception e) {
           e.printStackTrace();
       }
    }
}
