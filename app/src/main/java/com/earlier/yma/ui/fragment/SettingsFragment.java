/*
 * Copyright 2016 Namhyun, Gu
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

package com.earlier.yma.ui.fragment;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.earlier.yma.R;
import com.earlier.yma.data.model.preference.Time;
import com.earlier.yma.ui.preference.TimePickerPreference;
import com.google.gson.Gson;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set title
        getActivity().setTitle(R.string.action_settings);

        // Add resource
        addPreferencesFromResource(R.xml.pref_settings);

        // Bind Preference
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_default_menu_key)));

        // Set click listener
        Preference resetPreference = findPreference(getString(R.string.pref_reset_key));
        resetPreference.setOnPreferenceClickListener(this);

        // Set time picker listener
        TimePickerPreference breakfastPreference =
                (TimePickerPreference) findPreference(getString(R.string.pref_time_breakfast_key));
        breakfastPreference.setOnTimeSetListener(new TimePickerPreference.OnTimeSetListener() {
            @Override
            public boolean onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Time lunchTime = getTimeFromPreference(R.string.pref_time_lunch_key);
                Time currentTime = new Time(hourOfDay, minute);
                if (currentTime.compareTo(lunchTime) < 0)
                    return true;
                Toast.makeText(getActivity(),
                        getString(R.string.toast_cannot_save_time), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        TimePickerPreference lunchPreference =
                (TimePickerPreference) findPreference(getString(R.string.pref_time_lunch_key));
        lunchPreference.setOnTimeSetListener(new TimePickerPreference.OnTimeSetListener() {
            @Override
            public boolean onTimeSet(TimePicker view, int hourOfDay, int minute) {
                boolean enableBreakfastChanged =
                        getBooleanFromPreference(R.string.pref_breakfast_enable_key, false);
                Time breakfastTime = getTimeFromPreference(R.string.pref_time_breakfast_key);
                Time dinnerTime = getTimeFromPreference(R.string.pref_time_dinner_key);
                Time currentTime = new Time(hourOfDay, minute);

                boolean validation;
                if (enableBreakfastChanged) {
                    validation = (currentTime.compareTo(breakfastTime) > 0
                            && currentTime.compareTo(dinnerTime) < 0);
                } else {
                    validation = currentTime.compareTo(dinnerTime) < 0;
                }

                if (!validation) {
                    Toast.makeText(getActivity(), getString(R.string.toast_cannot_save_time),
                            Toast.LENGTH_SHORT).show();
                }
                return validation;
            }
        });

        TimePickerPreference dinnerPreference =
                (TimePickerPreference) findPreference(getString(R.string.pref_time_dinner_key));
        dinnerPreference.setOnTimeSetListener(new TimePickerPreference.OnTimeSetListener() {
            @Override
            public boolean onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Time lunchTime = getTimeFromPreference(R.string.pref_time_lunch_key);
                Time currentTime = new Time(hourOfDay, minute);
                if (currentTime.compareTo(lunchTime) > 0)
                    return true;
                Toast.makeText(getActivity(),
                        getString(R.string.toast_cannot_save_time), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private boolean getBooleanFromPreference(@StringRes int preferenceKey, boolean defaultValue) {
        return PreferenceManager
                .getDefaultSharedPreferences(getActivity()).getBoolean(getString(preferenceKey), defaultValue);
    }

    private Time getTimeFromPreference(@StringRes int preferenceKey) {
        String value = PreferenceManager
                .getDefaultSharedPreferences(getActivity()).getString(getString(preferenceKey), null);
        return new Gson().fromJson(value, Time.class);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.pref_reset_key))) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.pref_reset)
                    .content(R.string.dialog_reset_content)
                    .positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().apply();
                            getActivity().finish();
                            dialog.dismiss();
                        }
                    })
                    .negativeText(android.R.string.cancel)
                    .show();
        }
        return true;
    }
}
