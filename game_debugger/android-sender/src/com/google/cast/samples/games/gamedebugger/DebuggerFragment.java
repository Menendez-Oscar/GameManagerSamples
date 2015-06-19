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

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerClient.GameManagerResult;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.cast.games.PlayerInfo;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Collection;

/**
 * Main fragment for Lobby debugger.
 */
public class DebuggerFragment extends Fragment {

    public static final String TAG = "DebuggerFragment";

    private CastConnectionManager mCastConnectionManager;
    private GameManagerClient.Listener mListener = new DebuggerListener();

    private Button mAddPlayerButton;
    private Button mDisconnectButton;

    private TextView mTextViewApplicationName;
    private TextView mTextViewMaxPlayers;
    private TextView mTextViewGameplayState;
    private TextView mTextViewLobbyState;
    private TextView mTextViewGameStatusText;
    private TextView mTextViewGameData;
    private TextView mTextViewLastGameMessagePlayerId;
    private TextView mTextViewLastGameMessage;

    private ListView mListViewPlayers;
    private PlayerElementAdapter mPlayerListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mCastConnectionManager = GameDebuggerApplication.getInstance().getCastConnectionManager();

        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.debugger_fragment, container, false);

        mTextViewApplicationName = (TextView) view.findViewById(R.id.application_name);
        mTextViewMaxPlayers = (TextView) view.findViewById(R.id.max_players);
        mTextViewLobbyState = (TextView) view.findViewById(R.id.lobby_state);
        mTextViewGameplayState = (TextView) view.findViewById(R.id.gameplay_state);
        mTextViewGameStatusText = (TextView) view.findViewById(R.id.game_status_text);
        mTextViewGameData = (TextView) view.findViewById(R.id.game_data);
        mTextViewLastGameMessagePlayerId =
                (TextView) view.findViewById(R.id.last_game_message_player_id);
        mTextViewLastGameMessage = (TextView) view.findViewById(R.id.last_game_message);

        mAddPlayerButton = (Button) view.findViewById(R.id.button_add_player);
        mAddPlayerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GameDebuggerApplication.getInstance().getRequestInProgress()) {
                    mAddPlayerButton.setEnabled(false);

                    PendingResult<GameManagerResult> pendingResult =
                            mCastConnectionManager.getGameManagerClient().
                                    sendPlayerAvailableRequest(null, null);
                    pendingResult.setResultCallback(new ResultCallback<GameManagerResult>() {
                        @Override
                        public void onResult(GameManagerResult result) {
                            if (!result.getStatus().isSuccess()) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(result.getStatus().getStatusMessage())
                                        .setTitle(R.string.error_dialog_title_add_player)
                                        .create()
                                        .show();
                            }
                            GameDebuggerApplication.getInstance().setRequestInProgress(false);
                            mAddPlayerButton.setEnabled(
                                    mCastConnectionManager.isConnectedToReceiver());
                        }
                    });
                }
            }
        });

        mDisconnectButton = (Button) view.findViewById(R.id.button_disconnect);
        mDisconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCastConnectionManager.disconnectFromReceiver(true);
            }
        });

        mListViewPlayers = (ListView) view.findViewById(R.id.player_list);
        mPlayerListAdapter = new PlayerElementAdapter(getActivity());
        mListViewPlayers.setAdapter(mPlayerListAdapter);
        mListViewPlayers.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                return false;
            }
        });

        GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            GameManagerState state = gameManagerClient.getCurrentState();
            mTextViewApplicationName.setText(state.getApplicationName());
            mTextViewMaxPlayers.setText(Integer.toString(state.getMaxPlayers()));
            mTextViewLobbyState.setText(getLobbyStateName(state.getLobbyState()));
            mTextViewGameplayState.setText(getGameplayStateName(state.getGameplayState()));
            mTextViewGameStatusText.setText(state.getGameStatusText());

            JSONObject gameData = state.getGameData();
            String gameDataText = gameData != null ? gameData.toString() : "";
            mTextViewGameData.setText(gameDataText);
        }
        mAddPlayerButton.setEnabled(true);
        mDisconnectButton.setEnabled(true);
        mPlayerListAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(mListViewPlayers);
        return view;
    }

    @Override
    public void onResume() {
        if (mCastConnectionManager.isConnectedToReceiver()) {
            mCastConnectionManager.getGameManagerClient().setListener(mListener);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        GameManagerClient client = mCastConnectionManager.getGameManagerClient();
        if (client != null) {
            client.setListener(null);
        }
        super.onPause();
    }

    private static final int getLobbyStateName(int lobbyState) {
        switch (lobbyState) {
            case GameManagerClient.LOBBY_STATE_CLOSED:
                return R.string.lobby_state_closed;
            case GameManagerClient.LOBBY_STATE_OPEN:
                return R.string.lobby_state_open;
            case GameManagerClient.LOBBY_STATE_UNKNOWN:
            default:
                return R.string.lobby_state_unknown;
        }
    }

    private static final int getGameplayStateName(int gameplayState) {
        switch (gameplayState) {
            case GameManagerClient.GAMEPLAY_STATE_LOADING:
                return R.string.gameplay_state_loading;
            case GameManagerClient.GAMEPLAY_STATE_PAUSED:
                return R.string.gameplay_state_paused;
            case GameManagerClient.GAMEPLAY_STATE_RUNNING:
                return R.string.gameplay_state_running;
            case GameManagerClient.GAMEPLAY_STATE_SHOWING_INFO_SCREEN:
                return R.string.gameplay_state_showing_info_screen;
            case GameManagerClient.GAMEPLAY_STATE_UNKNOWN:
            default:
                return R.string.gameplay_state_unknown;
        }
    }

    private class DebuggerListener implements GameManagerClient.Listener {

        @Override
        public void onStateChanged(GameManagerState currentState, GameManagerState previousState) {
            if (currentState.hasLobbyStateChanged(previousState)) {
                Log.d(TAG, "onLobbyStateChange: " + currentState);
                mTextViewLobbyState.setText(getLobbyStateName(currentState.getLobbyState()));
            }
            if (currentState.hasGameplayStateChanged(previousState)) {
                Log.d(TAG, "onGameplayStateChanged: " + currentState);
                mTextViewGameplayState.setText(getGameplayStateName(
                        currentState.getGameplayState()));
            }
            if (currentState.hasGameDataChanged(previousState)) {
                String text = currentState.getGameData() != null
                        ? currentState.getGameData().toString() : "";
                Log.d(TAG, "onGameDataChanged: " + text);
                mTextViewGameData.setText(text);
            }
            if (currentState.hasGameStatusTextChanged(previousState)) {
                Log.d(TAG, "onGameStatusTextChanged: " + currentState.getGameStatusText());
                mTextViewGameStatusText.setText(currentState.getGameStatusText());
            }

            Collection<String> changedPlayers = currentState.getListOfChangedPlayers(previousState);
            for (String playerId : changedPlayers) {
                Log.d(TAG, "onPlayerChanged: " + playerId);

                PlayerInfo newPlayer = currentState.getPlayer(playerId);

                if (newPlayer != null) {
                    Log.d(TAG, "Player state: " + newPlayer.getPlayerState());
                    String playerData =
                            newPlayer.getPlayerData() != null
                                    ? newPlayer.getPlayerData().toString() : "";
                    Log.d(TAG, "Player data: " + playerData);
                }
            }
            if (changedPlayers.size() > 0) {
                mPlayerListAdapter.recreateDataSet();
                setListViewHeightBasedOnChildren(mListViewPlayers);
            }
        }

        @Override
        public void onGameMessageReceived(String playerId, JSONObject message) {
            String text = message != null ? message.toString() : "";
            Log.d(TAG, "onGameMessageReceived for player:" + playerId + " " + text);
            mTextViewLastGameMessagePlayerId.setText(playerId);
            mTextViewLastGameMessage.setText(text);
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
