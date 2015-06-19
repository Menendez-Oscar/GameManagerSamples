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

import android.app.Application;

/**
 * The application class.
 */
public class GameDebuggerApplication extends Application
        implements CastConnectionManager.CastAppIdProvider {

    private static GameDebuggerApplication sInstance;

    private CastConnectionManager.CastAppIdProvider mCastAppIdProvider = null;
    private CastConnectionManager mCastConnectionManager;
    private boolean mRequestInProgress = false;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mCastConnectionManager = new CastConnectionManager(this, this);
    }

    public static GameDebuggerApplication getInstance() {
        return sInstance;
    }

    public CastConnectionManager getCastConnectionManager() {
        return mCastConnectionManager;
    }

    public void setCastAppIdProvider(CastConnectionManager.CastAppIdProvider appIdProvider) {
        mCastAppIdProvider = appIdProvider;
    }

    /**
     * Sets whether there is a pending result for an active request. This is used to disable
     * buttons while we are waiting for the receiver to process another request.
     */
    public void setRequestInProgress(boolean value) {
        mRequestInProgress = value;
    }

    /**
     * Returns whether there is a pending result for an active request. This is used to disable
     * buttons while we are waiting for the receiver to process another request.
     */
    public boolean getRequestInProgress() {
        return mRequestInProgress;
    }

    @Override
    public String getCastAppId() {
        if (mCastAppIdProvider == null) {
            return getResources().getString(R.string.app_id);
        }
        return mCastAppIdProvider.getCastAppId();
    }
}
