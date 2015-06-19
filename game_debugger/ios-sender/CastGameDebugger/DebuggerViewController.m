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

#import "AppDelegate.h"
#import "DebuggerViewController.h"
#import "DeviceTableViewController.h"
#import "UIButton+Extensions.h"

@interface DebuggerViewController() {
  NSInteger _editedPlayerIndex;
  GCKPlayerState _editedPlayerState;
  BOOL _isAddingExtraData;
  id _addedExtraData;
  NSDictionary *_playerStateMappings;
  NSArray *_playerStateTransitionsWhileConnected;
  NSArray *_playerStateTransitionsWhileDisconnected;
  NSDictionary *_lobbyStateMappings;
  NSDictionary *_gameplayStateMappings;

  GCKGameManagerChannel *_gameManagerChannel;
}

@end

@implementation DebuggerViewController

#pragma mark - Constants

static NSString *const kCellIdForPlayer = @"player";
static int const kTagForPlayerName = 100;
static int const kTagForPlayerStatus = 101;
static int const kTagForPlayerData = 102;
static int const kTagForEditButton = 103;
static int const kTagForSendGameRequestButton = 104;

#pragma mark - UIViewController

- (void)viewDidLoad {
  [super viewDidLoad];

  // Become delegate for ChromecastDeviceController
  AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
  delegate.chromecastDeviceController.delegate = self;

  // Become delegate for GCKGameManagerChannel
  _gameManagerChannel = [[GCKGameManagerChannel alloc]
                         initWithSessionID:[[delegate chromecastDeviceController] sessionID]];
  _gameManagerChannel.delegate = self;
  [delegate.chromecastDeviceController.deviceManager addChannel:_gameManagerChannel];

  // Map player states to UI strings.
  _playerStateMappings = @{
    @(GCKPlayerStateUnknown) : @"Unknown",
    @(GCKPlayerStateDropped) : @"Dropped",
    @(GCKPlayerStateQuit) : @"Quit",
    @(GCKPlayerStateAvailable) : @"Available",
    @(GCKPlayerStateReady) : @"Ready",
    @(GCKPlayerStateIdle) : @"Idle",
    @(GCKPlayerStatePlaying) : @"Playing",
  };

  // List of valid player state transitions while connected.
  _playerStateTransitionsWhileConnected = @[@(GCKPlayerStateAvailable), @(GCKPlayerStateReady),
                                            @(GCKPlayerStatePlaying), @(GCKPlayerStateIdle),
                                            @(GCKPlayerStateQuit)];

  // List of valid player state transitions while disconnected.
  _playerStateTransitionsWhileDisconnected = @[@(GCKPlayerStateAvailable)];

  // Map lobby states to UI strings.
  _lobbyStateMappings = @{
    @(GCKLobbyStateUnknown) : @"Unknown",
    @(GCKLobbyStateOpen) : @"Open",
    @(GCKLobbyStateClosed) : @"Closed",
  };

  // Map gameplay states to UI strings.
  _gameplayStateMappings = @{
    @(GCKGameplayStateUnknown) : @"Unknown",
    @(GCKGameplayStateLoading) : @"Loading",
    @(GCKGameplayStateRunning) : @"Running",
    @(GCKGameplayStatePaused) : @"Paused",
    @(GCKGameplayStateShowingInfoScreen) : @"Showing Info Screen",
  };
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewDidLoad];

  [self.gameDataTextView.textContainer setLineFragmentPadding:0];
  [self.gameDataTextView setTextContainerInset:UIEdgeInsetsZero];

  if ([self.playerTableView respondsToSelector:@selector(setSeparatorInset:)]) {
    [self.playerTableView setSeparatorInset:UIEdgeInsetsZero];
  }

  self.playerTableView.contentInset = UIEdgeInsetsMake(-5.0f, 0.0f, 0.0f, 0.0f);
  [self.gameDataTextView setContentOffset:CGPointZero animated:NO];
}

- (void)viewDidAppear:(BOOL)animated {
  [self.gameDataTextView setContentOffset:CGPointZero animated:NO];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
}

- (BOOL)prefersStatusBarHidden {
  return YES;
}

#pragma mark - ChromecastDeviceDelegate

- (void)willConnectToDevice:(GCKDevice *)device {
  NSLog(@"Will connect to device");
}

- (void)didConnectToDevice:(GCKDevice *)device {
  NSLog(@"Have connected to device");
}

- (void)didDisconnect {
  NSLog(@"Did disconnect from device");
  [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - IBActions

- (IBAction)openCastMenu:(id)sender {
  UIStoryboard *storyboard = self.storyboard;
  DeviceTableViewController *dvc =
      [storyboard instantiateViewControllerWithIdentifier:@"DeviceTableView"];
  [self presentViewController:dvc animated:YES completion:nil];
}

- (IBAction)addPlayer:(id)sender {
  NSLog(@"Add player");
  [self addPlayer];
}

- (IBAction)editButtonTapped:(id)sender event:(id)event {
  NSSet *touches = [event allTouches];
  UITouch *touch = [touches anyObject];
  CGPoint currentTouchPosition = [touch locationInView:self.playerTableView];
  NSIndexPath *indexPath = [self.playerTableView indexPathForRowAtPoint: currentTouchPosition];

  if (indexPath) {
    [self tableView:self.playerTableView accessoryButtonTappedForRowWithIndexPath:indexPath];
  }
}

- (IBAction)extraDataButtonTapped:(id)sender {
  [self addExtraData];
}

- (IBAction)doneEditingButtonTapped:(id)sender {
  GCKGameManagerState *currentState = _gameManagerChannel.currentState;
  GCKPlayerInfo *playerInfo = currentState.players[_editedPlayerIndex];
  NSArray *playerStates = [self getPlayerStateTransitions:playerInfo];
  NSNumber *newPlayerStateNumber =
      playerStates[[self.playerStatePickerView selectedRowInComponent:0]];
  _editedPlayerState = [newPlayerStateNumber integerValue];

  [self saveEditedPlayerIndex:_editedPlayerIndex playerState:_editedPlayerState];
}

- (IBAction)sendGameRequestButtonTapped:(id)sender event:(id)event{
  NSSet *touches = [event allTouches];
  UITouch *touch = [touches anyObject];
  CGPoint currentTouchPosition = [touch locationInView:self.playerTableView];
  NSIndexPath *indexPath = [self.playerTableView indexPathForRowAtPoint: currentTouchPosition];

  if (!indexPath) {
    return;
  }

  [self startSendGameRequest:indexPath.row];
}

#pragma mark - UiTableView data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
  // Return the number of sections.
  return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  GCKGameManagerState *currentState = _gameManagerChannel.currentState;
  if ([_gameManagerChannel isInitialConnectionEstablished]) {
    return currentState.players.count;
  } else {
    return 0;
  }
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  UITableViewCell *cell;
  cell = [tableView dequeueReusableCellWithIdentifier:kCellIdForPlayer forIndexPath:indexPath];
  UILabel *playerName = (UILabel *)[cell.contentView viewWithTag:kTagForPlayerName];
  UILabel *playerStatus = (UILabel *)[cell.contentView viewWithTag:kTagForPlayerStatus];
  UILabel *playerData = (UILabel *)[cell.contentView viewWithTag:kTagForPlayerData];
  UIButton *editButton = (UIButton *)[cell.contentView viewWithTag:kTagForEditButton];
  UIButton *sendButton = (UIButton *)[cell.contentView viewWithTag:kTagForSendGameRequestButton];
  GCKGameManagerState *currentState = _gameManagerChannel.currentState;

  // Show all players but only make controllable players editable.
  NSArray *players = currentState.players;
  if (players && indexPath.row < players.count) {
    GCKPlayerInfo *player = players[indexPath.row];
    playerName.text = player.playerID;
    playerStatus.text = [_playerStateMappings objectForKey:@(player.playerState)];
    playerData.text = [self parseJSONData:player.playerData];
    [editButton setHitTestEdgeInsets:UIEdgeInsetsMake(-15, -15, -15, -15)];
    editButton.hidden = !player.isControllable;
    sendButton.hidden = !player.isControllable;
  }

  return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  return self.playerTableView.rowHeight;
}

#pragma mark - UITableView delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
  // NOP
}

- (void)tableView:(UITableView *)tableView
    accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath {
  NSLog(@"Accessory button tapped %@", indexPath);
  [self startEditPlayer:indexPath.row];
}

#pragma mark - UIPickerView data source

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component {
  return 40;
}

- (NSString *)pickerView:(UIPickerView *)pickerView
             titleForRow:(NSInteger)row
            forComponent:(NSInteger)component {
  GCKGameManagerState *currentState = _gameManagerChannel.currentState;
  GCKPlayerInfo *playerInfo = currentState.players[_editedPlayerIndex];
  NSArray *playerStates = [self getPlayerStateTransitions:playerInfo];
  return (NSString *)[_playerStateMappings objectForKey:playerStates[row]];
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
  GCKGameManagerState *currentState = _gameManagerChannel.currentState;
  if (![_gameManagerChannel isInitialConnectionEstablished] || !currentState.players.count) {
    return 0;
  }
  GCKPlayerInfo *playerInfo = currentState.players[_editedPlayerIndex];
  NSArray *playerStates = [self getPlayerStateTransitions:playerInfo];
  return playerStates.count;
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
  return 1;
}

#pragma mark - UIPickerView delegate

- (void)pickerView:(UIPickerView *)pickerView
      didSelectRow:(NSInteger)row
       inComponent:(NSInteger)component {
  GCKGameManagerState *currentState = _gameManagerChannel.currentState;
  GCKPlayerInfo *playerInfo = currentState.players[_editedPlayerIndex];
  NSArray *playerStates = [self getPlayerStateTransitions:playerInfo];
  NSNumber *newPlayerStateNumber = playerStates[row];
  _editedPlayerState = [newPlayerStateNumber integerValue];
}

#pragma mark - GCKGameManagerChannel delegate

- (void)gameManagerChannel:(GCKGameManagerChannel *)gameManagerChannel
          stateDidChangeTo:(GCKGameManagerState *)currentState
                      from:(GCKGameManagerState *)previousState {
  if ([currentState hasLobbyStateChanged:previousState]) {
    NSLog(@"lobbyStateDidChangeTo:%zd from:%zd", currentState, previousState);
    self.lobbyStateLabel.text = [_lobbyStateMappings objectForKey:@(currentState.lobbyState)];
  }
  if ([currentState hasGameplayStateChanged:previousState]) {
    NSLog(@"gameplayStateDidChangeTo:%zd from:%zd", currentState, previousState);
    self.gameplayStateLabel.text =
        [_gameplayStateMappings objectForKey:@(currentState.gameplayState)];
  }
  if ([currentState hasGameDataChanged:previousState]) {
    id currentGameData = currentState.gameData;
    id previousGameData = previousState.gameData;
    NSLog(@"gameDataDidChangeTo:%@ from:%@", currentGameData, previousGameData);
    if (!currentGameData) {
      self.gameDataTextView.text = @"";
      return;
    }

    self.gameDataTextView.text = [self parseJSONData:currentGameData];
  }
  if ([currentState hasGameStatusTextChanged:previousState]) {
    NSString *currentGameStatusText = currentState.gameStatusText;
    NSString *previousGameStatusText = previousState.gameStatusText;
    NSLog(@"gameStatusTextDidChangeTo:%@ from:%@", currentGameStatusText, previousGameStatusText);
    self.gameStatusLabel.text = currentGameStatusText;
  }

  NSArray *changedPlayerIDs = [currentState getListOfChangedPlayers:previousState];
  for (NSString *changedPlayerID in changedPlayerIDs) {
    GCKPlayerInfo *currentPlayer = [currentState getPlayer:changedPlayerID];
    GCKPlayerInfo *previousPlayer = [currentState getPlayer:changedPlayerID];
    NSLog(@"playerInfoDidChangeTo:%@ from:%@", currentPlayer, previousPlayer);
  }
  if (changedPlayerIDs.count > 0) {
    [self.playerTableView reloadData];
  }
}

- (void)gameManagerChannel:(GCKGameManagerChannel *)gameManagerChannel
    didReceiveGameMessage:(id)gameMessage
               forPlayerID:(NSString *)playerID {
  NSLog(@"didReceiveGameMessage:%@ forPlayerID:%@", gameMessage, playerID);
  if (!gameMessage) {
    self.gameMessageTextView.text = @"";
    return;
  }
  self.gameMessageTextView.text = [self parseJSONData:gameMessage];
}

- (void)gameManagerChannel:(GCKGameManagerChannel *)gameManagerChannel
   requestDidSucceedWithID:(NSInteger)requestID
                    result:(GCKGameManagerResult *)result {
  NSLog(@"requestDidSucceedWithID:%zd result:%@", requestID, result);
}

- (void)gameManagerChannel:(GCKGameManagerChannel *)gameManagerChannel
      requestDidFailWithID:(NSInteger)requestID
                     error:(NSError *)error {
  NSLog(@"requestDidFailWithID:%zd result:%@", requestID, error);
  NSString *errorTitle = @"GCKGameManagerChannel Error";
  UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(errorTitle, nil)
                                                  message:NSLocalizedString(error.description, nil)
                                                 delegate:nil
                                        cancelButtonTitle:NSLocalizedString(@"OK", nil)
                                        otherButtonTitles:nil];
  [alert show];
}

- (void)gameManagerChannelDidConnect:(GCKGameManagerChannel *)gameManagerChannel {
  GCKGameManagerState *currentState = gameManagerChannel.currentState;
  NSLog(@"gameManagerChannelDidConnect. applicationName:%@ maxPlayers:%ld",
        currentState.applicationName, (long)currentState.maxPlayers);
  self.applicationNameLabel.text = currentState.applicationName;
  self.maxPlayersLabel.text = [@(currentState.maxPlayers) stringValue];
}

- (void)gameManagerChannel:(GCKGameManagerChannel *)gameManagerChannel
    didFailToConnectWithError:(NSError *)error {
  NSLog(@"didFailToConnectWithError:%@", error);
  NSString *errorTitle = @"GCKGameManagerChannel Error";
  UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(errorTitle, nil)
                                                  message:NSLocalizedString(error.description, nil)
                                                 delegate:nil
                                        cancelButtonTitle:NSLocalizedString(@"OK", nil)
                                        otherButtonTitles:nil];
  [alert show];
}

#pragma mark - Player Adding / Editing / Messaging

- (void)addPlayer {
  [_gameManagerChannel sendPlayerAvailableRequest:nil playerID:nil];
}

- (void)startEditPlayer:(NSInteger)index {
  // Set the initial edited player state to the first player state transition.
  GCKGameManagerState *currentState = _gameManagerChannel.currentState;
  GCKPlayerInfo *playerInfo = currentState.players[index];
  NSArray *playerStates = [self getPlayerStateTransitions:playerInfo];
  NSNumber *newPlayerStateNumber = playerStates[0];
  _editedPlayerState = newPlayerStateNumber.integerValue;


  self.playerStateEditorView.hidden = NO;
  self.playerStateEditorView.frame = CGRectMake(0, self.view.bounds.size.height,
                                                self.playerStateEditorView.bounds.size.width,
                                                self.playerStateEditorView.bounds.size.height);

  // Refresh picker, which will load player state transitions using _editedPlayerIndex.
  _editedPlayerIndex = index;
  [self.playerStatePickerView reloadAllComponents];
  [self.playerStatePickerView selectRow:0 inComponent:0 animated:false];
  [UIView animateWithDuration:0.3
                   animations:^{
                       self.playerStateEditorView.frame =
                         CGRectMake(0,
                                    self.view.bounds.size.height -
                                      self.playerStateEditorView.bounds.size.height,
                                    self.playerStateEditorView.bounds.size.width,
                                    self.playerStateEditorView.bounds.size.height);
                   }
                   completion:^(BOOL finished) {
                       self.playerStatePickerView.delegate = self;
                   }
  ];
}

- (void)saveEditedPlayerIndex:(NSInteger)playerIndex playerState:(GCKPlayerState)playerState {
  GCKGameManagerState *currentState = _gameManagerChannel.currentState;
  GCKPlayerInfo *playerInfo = currentState.players[playerIndex];
  switch (playerState) {
    case GCKPlayerStateAvailable:
      [_gameManagerChannel sendPlayerAvailableRequest:_addedExtraData playerID:playerInfo.playerID];
      break;
    case GCKPlayerStateReady:
      [_gameManagerChannel sendPlayerReadyRequest:_addedExtraData playerID:playerInfo.playerID];
      break;
    case GCKPlayerStatePlaying:
      [_gameManagerChannel sendPlayerPlayingRequest:_addedExtraData playerID:playerInfo.playerID];
      break;
    case GCKPlayerStateIdle:
      [_gameManagerChannel sendPlayerIdleRequest:_addedExtraData playerID:playerInfo.playerID];
      break;
    case GCKPlayerStateQuit:
      [_gameManagerChannel sendPlayerQuitRequest:_addedExtraData playerID:playerInfo.playerID];
      break;
    default:
      NSLog(@"Cannot save player state - unsupported player state transition to: %ld", playerState);
      break;
  }
  _addedExtraData = nil;
  self.playerStatePickerView.delegate = nil;

  [UIView animateWithDuration:0.3
                   animations:^{
                       self.playerStateEditorView.frame =
                         CGRectMake(0,
                                    self.view.bounds.size.height,
                                    self.playerStateEditorView.bounds.size.width,
                                    self.playerStateEditorView.bounds.size.height);
                   }
                   completion:^(BOOL finished) {
                       self.playerStateEditorView.hidden = YES;
                   }
  ];
}

- (void)addExtraData {
  _isAddingExtraData = YES;
  UIAlertView *inputAlert = [[UIAlertView alloc]
          initWithTitle:NSLocalizedString(@"Add extraData with state change", nil)
                message:NSLocalizedString(@"Enter JSON data", nil)
               delegate:self
      cancelButtonTitle:NSLocalizedString(@"Add", nil)
      otherButtonTitles:nil];
  [inputAlert setAlertViewStyle:UIAlertViewStylePlainTextInput];
  [inputAlert show];
}

- (void)startSendGameRequest:(NSInteger)index {
  _editedPlayerIndex = index;
  _isAddingExtraData = NO;
  UIAlertView *inputAlert = [[UIAlertView alloc]
          initWithTitle:NSLocalizedString(@"Send game request", nil)
                message:NSLocalizedString(@"Enter JSON request", nil)
               delegate:self
      cancelButtonTitle:NSLocalizedString(@"Send", nil)
      otherButtonTitles:nil];
  [inputAlert setAlertViewStyle:UIAlertViewStylePlainTextInput];
  [inputAlert show];
}

#pragma mark - UIAlertView delegate for adding extra data or sending game request

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
  NSString *message = [alertView textFieldAtIndex:0].text;
  if (![message length]) {
    _isAddingExtraData = NO;
    return;
  }

  NSError *error;
  NSData *JSONData = [message dataUsingEncoding:NSUTF8StringEncoding];
  NSDictionary *JSONDictionary = [NSJSONSerialization JSONObjectWithData:JSONData
                                                                 options:kNilOptions
                                                                   error:&error];
  if (error) {
    NSLog(@"Error serializing game message: %@", error);
    NSString *errorTitle = @"NSJSONSerialization Error";
    UIAlertView *alert = [[UIAlertView alloc]
            initWithTitle:NSLocalizedString(errorTitle, nil)
                  message:NSLocalizedString(error.description, nil)
                 delegate:nil
        cancelButtonTitle:NSLocalizedString(@"OK", nil)
        otherButtonTitles:nil];
    [alert show];
    _isAddingExtraData = NO;
    return;
  }

  if (_isAddingExtraData) {
    _addedExtraData = JSONDictionary;
  } else {
    GCKGameManagerState *currentState = _gameManagerChannel.currentState;
    GCKPlayerInfo *playerInfo = currentState.players[_editedPlayerIndex];
    [_gameManagerChannel sendGameRequest:JSONDictionary playerID:playerInfo.playerID];
  }
  _isAddingExtraData = NO;
}

#pragma mark Stop Application

- (IBAction)stopReceiverApplication:(id)sender {
  AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
  [delegate.chromecastDeviceController stopAndDisconnectFromDevice];
}

#pragma mark Utilities

- (NSString *)parseJSONData:(id)JSONData {
  if (!JSONData || JSONData == [NSNull null]) {
    return @"";
  }

  NSError *error;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:JSONData
                                                     options:0
                                                       error:&error];
  if (error) {
    NSLog(@"error parsing JSON data: %@", error.localizedDescription);
    return @"JSON parsing error!";
  }

  return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

- (NSArray *)getPlayerStateTransitions:(GCKPlayerInfo *)playerInfo {
  if (playerInfo.isConnected) {
    return _playerStateTransitionsWhileConnected;
  } else {
    return _playerStateTransitionsWhileDisconnected;
  }
}

@end
