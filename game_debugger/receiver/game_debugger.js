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
goog.provide('cast.games.gamedebugger.GameDebuggerGame');

goog.require('cast.games.common.receiver.Game');



/**
 * The game manager debugger. Shows how to add the debug UI to a receiver.
 *
 * @param {!cast.receiver.games.GameManager} GameManager
 * @implements {cast.games.common.receiver.Game}
 * @constructor
 * @export
 */
cast.games.gamedebugger.GameDebuggerGame =
    function(GameManager) {
  /** @private {!cast.receiver.games.GameManager} */
  this.gameManager_ = GameManager;

  /**
   * You can access this in the dev console if you want to play around with
   * the debug UI although in this sample, we automatically open the debug UI.
   * @public {!cast.receiver.games.debug.DebugUI}
   */
  this.debugUi = new cast.receiver.games.debug.DebugUI(GameManager);
};


/**
 * Runs the game and automatically shows the debug UI. Game should load if not
 * loaded yet.
 * @param {function()} loadedCallback This function will be called when the game
 *     finishes loading or is already loaded and about to actually run.
 * @export
 */
cast.games.gamedebugger.GameDebuggerGame.prototype.run =
    function(loadedCallback) {
  this.gameManager_.updateGameplayState(
      cast.receiver.games.GameplayState.RUNNING, null);
  loadedCallback();
  this.debugUi.open();
};


/**
 * Stops the game and closes the debug UI.
 * @export
 */
cast.games.gamedebugger.GameDebuggerGame.prototype.stop =
    function() {
  this.debugUi.close();
};
