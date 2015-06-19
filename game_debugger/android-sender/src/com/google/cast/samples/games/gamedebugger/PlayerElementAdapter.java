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
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.cast.games.PlayerInfo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom adapter for displaying players in the game.
 */
public class PlayerElementAdapter extends BaseAdapter {

    static class ViewHolder {
        public TextView playerIdTextView;
        public TextView playerStateLabelTextView;
        public TextView playerStateTextView;
        public TextView playerDataTextView;
        public TextView playerNotControllableIndicator;
        public TextView gameFullIndicator;
        public Button transitionButton;
        public Button sendGameMessageButton;
    }

    private final Activity mActivity;
    private final CastConnectionManager mCastConnectionManager;
    private final LayoutInflater mLayoutInflator;

    private ArrayList<PlayerInfo> mPlayers;

    public PlayerElementAdapter(Activity activity) {
        super();
        mActivity = activity;
        mCastConnectionManager = GameDebuggerApplication.getInstance().getCastConnectionManager();
        mLayoutInflator = (LayoutInflater) activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        recreateDataSet();
    }

    public void recreateDataSet() {
        mPlayers = new ArrayList<>();
        if (mCastConnectionManager.getGameManagerClient() != null) {
            mPlayers.addAll(
                    mCastConnectionManager.getGameManagerClient().getCurrentState().getPlayers());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPlayers.size();
    }

    @Override
    public PlayerInfo getItem(int position) {
        return mPlayers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final PlayerInfo player = getItem(position);
        final String playerId = player.getPlayerId();

        if (convertView == null) {
            convertView = mLayoutInflator.inflate(R.layout.player_element, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.playerIdTextView = (TextView) convertView.findViewById(R.id.player_id);
            viewHolder.playerStateTextView = (TextView) convertView.findViewById(R.id.player_state);
            viewHolder.playerStateLabelTextView = (TextView) convertView.findViewById(
                    R.id.player_state_label);
            viewHolder.playerDataTextView = (TextView) convertView.findViewById(R.id.player_data);
            viewHolder.playerNotControllableIndicator = (TextView) convertView.findViewById(
                    R.id.player_not_controllable_indicator);
            viewHolder.gameFullIndicator = (TextView) convertView.findViewById(
                    R.id.game_full_indicator);
            viewHolder.transitionButton = (Button) convertView.findViewById(
                    R.id.button_player_transition);
            viewHolder.sendGameMessageButton = (Button) convertView.findViewById(
                    R.id.button_send_game_message);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        OnClickListener transitionClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GameDebuggerApplication.getInstance().getRequestInProgress()) {
                    PlayerTransitionDialogFragment dialogFragment =
                            PlayerTransitionDialogFragment.newInstance(playerId, true);
                    dialogFragment.show(mActivity.getFragmentManager(),
                            "transition_player_dialog");
                }
            }
        };

        OnClickListener sendGameMessageClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GameDebuggerApplication.getInstance().getRequestInProgress()) {
                    PlayerTransitionDialogFragment dialogFragment =
                            PlayerTransitionDialogFragment.newInstance(playerId, false);
                    dialogFragment.show(mActivity.getFragmentManager(),
                            "transition_player_dialog");
                }
            }
        };

        // Populate the values.
        viewHolder.playerIdTextView.setText(playerId);
        viewHolder.playerStateTextView.setText(getPlayerStateName(player.getPlayerState()));
        String playerData = player.getPlayerData() != null ? player.getPlayerData().toString() : "";
        viewHolder.playerDataTextView.setText(playerData);

        // Add on click listeners.
        viewHolder.transitionButton.setOnClickListener(transitionClickListener);
        viewHolder.playerStateTextView.setOnClickListener(transitionClickListener);
        viewHolder.playerStateLabelTextView.setOnClickListener(transitionClickListener);
        viewHolder.sendGameMessageButton.setOnClickListener(sendGameMessageClickListener);


        if (mCastConnectionManager.getGameManagerClient() != null) {
            GameManagerState state =
                    mCastConnectionManager.getGameManagerClient().getCurrentState();
            PlayerInfo playerInfo = state.getPlayer(playerId);
            if (playerInfo.isControllable() && !playerInfo.isConnected()
                    && state.getConnectedPlayers().size() >= state.getMaxPlayers()) {
                viewHolder.gameFullIndicator.setVisibility(View.VISIBLE);
                viewHolder.playerNotControllableIndicator.setVisibility(View.GONE);
                viewHolder.transitionButton.setVisibility(View.GONE);
                viewHolder.sendGameMessageButton.setVisibility(View.GONE);
                viewHolder.playerStateTextView.setClickable(false);
                viewHolder.playerStateLabelTextView.setClickable(false);
            } else if (playerInfo.isControllable()) {
                viewHolder.gameFullIndicator.setVisibility(View.GONE);
                viewHolder.playerNotControllableIndicator.setVisibility(View.GONE);
                viewHolder.transitionButton.setVisibility(View.VISIBLE);
                viewHolder.sendGameMessageButton.setVisibility(View.VISIBLE);
                viewHolder.playerStateTextView.setClickable(true);
                viewHolder.playerStateLabelTextView.setClickable(true);
            } else {
                viewHolder.gameFullIndicator.setVisibility(View.GONE);
                viewHolder.playerNotControllableIndicator.setVisibility(View.VISIBLE);
                viewHolder.transitionButton.setVisibility(View.GONE);
                viewHolder.sendGameMessageButton.setVisibility(View.GONE);
                viewHolder.playerStateTextView.setClickable(false);
                viewHolder.playerStateLabelTextView.setClickable(false);
            }
        }
        return convertView;
    }

    private static final int getPlayerStateName(int playerState) {
        switch (playerState) {
            case GameManagerClient.PLAYER_STATE_AVAILABLE:
                return R.string.player_state_available;
            case GameManagerClient.PLAYER_STATE_READY:
                return R.string.player_state_ready;
            case GameManagerClient.PLAYER_STATE_IDLE:
                return R.string.player_state_idle;
            case GameManagerClient.PLAYER_STATE_PLAYING:
                return R.string.player_state_playing;
            case GameManagerClient.PLAYER_STATE_QUIT:
                return R.string.player_state_quit;
            case GameManagerClient.PLAYER_STATE_DROPPED:
                return R.string.player_state_dropped;
            case GameManagerClient.PLAYER_STATE_UNKNOWN:
            default:
                return R.string.player_state_unknown;
        }
    }
}
