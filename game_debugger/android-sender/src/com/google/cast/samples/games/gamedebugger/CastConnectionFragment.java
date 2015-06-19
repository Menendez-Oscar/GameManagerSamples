// Copyright 2015 Google Inc. All Rights Reserved.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.cast.samples.games.gamedebugger;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Observable;
import java.util.Observer;

/**
 * A fragment displayed while this application is not yet connected to a cast device. It allows the
 * user to select the cast app ID.
 */
public class CastConnectionFragment
        extends Fragment implements Observer, CastConnectionManager.CastAppIdProvider {
    public static final String TAG = "CastConnectionFragment";

    private static final String APP_ID_PREFERENCE_KEY = "app_id";

    private CastConnectionManager mCastConnectionManager;

    private View mAppIdSelector;
    private View mSpinner;
    private EditText mAppIdEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mCastConnectionManager = GameDebuggerApplication.getInstance().getCastConnectionManager();
        mCastConnectionManager.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cast_connection_fragment, container, false);
        mAppIdSelector = view.findViewById(R.id.app_id_selector);
        mSpinner = view.findViewById(R.id.spinner);
        mAppIdEditText = (EditText) view.findViewById(R.id.app_id_edit_text);

        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String appId = preferences.getString(APP_ID_PREFERENCE_KEY, "");
        mAppIdEditText.setText(appId);

        return view;
    }

    @Override
    public void onPause() {
        mCastConnectionManager.deleteObserver(this);

        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        preferences.edit()
                .putString(APP_ID_PREFERENCE_KEY, mAppIdEditText.getText().toString())
                .apply();

        super.onPause();
    }

    @Override
    public void update(Observable object, Object data) {
        if (getView() == null) {
            return;
        }
        if (mCastConnectionManager.getSelectedDevice() != null) {
            mAppIdSelector.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
        } else {
            mAppIdSelector.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public String getCastAppId() {
        // If the text field is empty, use the default app Id.
        if (mAppIdEditText.getText().toString().length() == 0) {
            return getResources().getString(R.string.app_id);
        }
        return mAppIdEditText.getText().toString();
    }
}
