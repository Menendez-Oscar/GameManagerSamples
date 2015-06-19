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
'use strict';

/** @suppress {extraRequire} Needed to set up global command docs. */
goog.require('cast.games.common.sender.CommandDocs');
goog.require('cast.games.common.sender.debugGameManagerClient');
goog.require('cast.games.common.sender.setup');


/** @define {string} Application ID used when running the sender. */
var APP_ID = 'F393D32D';

// Global reference to game session manager for console debugging.
var gameManagerClient = null;


/**
 * Request a cast session when Cast Sender API loads.
 * @param {boolean} loaded
 * @param {Object} errorInfo
 */
window['__onGCastApiAvailable'] = function(loaded, errorInfo) {
  if (!loaded) {
    console.error('### Cast Sender SDK failed to load:');
    console.dir(errorInfo);
    return;
  }

  cast.games.common.sender.setup(APP_ID, onSessionReady_);
};


/**
 * Callback when a cast session is ready. Connects the game manager.
 * @param {!chrome.cast.Session} session
 * @private
 */
var onSessionReady_ = function(session) {
  console.log('### Creating game manager client.');
  chrome.cast.games.GameManagerClient.getInstanceFor(session,
      function(result) {
        gameManagerClient = result.gameManagerClient;
        cast.games.common.sender.debugGameManagerClient(gameManagerClient);
        console.log('### Game manager client initialized!');
        help();
      },
      function(error) {
        console.error('### Error initializing the game manager client: ' +
            error.errorDescription + ' ' +
            'Error code: ' + error.errorCode);
      });
};
