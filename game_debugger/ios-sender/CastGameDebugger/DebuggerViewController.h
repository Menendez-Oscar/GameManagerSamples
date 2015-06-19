// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#import <GoogleCastGames/GoogleCastGames.h>
#import <UIKit/UIKit.h>

#import "ChromecastDeviceController.h"

@interface DebuggerViewController :
    UIViewController <ChromecastControllerDelegate, GCKGameManagerChannelDelegate,
                      UITableViewDataSource, UIPickerViewDelegate, UIPickerViewDataSource,
                      UITableViewDelegate, UIAlertViewDelegate>

@property(nonatomic) IBOutlet UILabel *applicationNameLabel;
@property(nonatomic) IBOutlet UILabel *maxPlayersLabel;
@property(nonatomic) IBOutlet UILabel *gameplayStateLabel;
@property(nonatomic) IBOutlet UILabel *lobbyStateLabel;
@property(nonatomic) IBOutlet UILabel *gameStatusLabel;
@property(nonatomic) IBOutlet UITextView *gameDataTextView;
@property(nonatomic) IBOutlet UITextView *gameMessageTextView;

@property(nonatomic) IBOutlet UITableView *playerTableView;
@property(nonatomic) IBOutlet UIView *playerStateEditorView;
@property(nonatomic) IBOutlet UIPickerView *playerStatePickerView;

/** Cast menu button **/
- (IBAction)openCastMenu:(id)sender;
/** AddPlayer button **/
- (IBAction)addPlayer:(id)sender;
/** EditPlayer button **/
- (IBAction)editButtonTapped:(id)sender event:(id)event;
/** ExtraData button **/
- (IBAction)extraDataButtonTapped:(id)sender;
/** DoneEditing button **/
- (IBAction)doneEditingButtonTapped:(id)sender;
/** SendGameRequest button **/
- (IBAction)sendGameRequestButtonTapped:(id)sender event:(id)event;
/** Stop receiver application button **/
- (IBAction)stopReceiverApplication:(id)sender;

@end
