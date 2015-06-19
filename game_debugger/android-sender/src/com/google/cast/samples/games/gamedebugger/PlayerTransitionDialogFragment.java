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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This dialog fragment is used to transition players to other states and send game messages.
 */
public class PlayerTransitionDialogFragment extends DialogFragment
        implements TextView.OnEditorActionListener {

    public static final String TAG = "PlayerTransitionDialog";

    private static final String KEY_PLAYER_ID = "playerId";
    private static final String KEY_IS_TRANSITION = "isTransition";

    private static final int[] PLAYER_STATES = {
            GameManagerClient.PLAYER_STATE_AVAILABLE,
            GameManagerClient.PLAYER_STATE_READY,
            GameManagerClient.PLAYER_STATE_IDLE,
            GameManagerClient.PLAYER_STATE_PLAYING,
            GameManagerClient.PLAYER_STATE_QUIT,
    };

    private static final int[] PLAYER_STATE_RESOURCE_IDS = {
            R.string.player_state_available,
            R.string.player_state_ready,
            R.string.player_state_idle,
            R.string.player_state_playing,
            R.string.player_state_quit,
    };

    private LinearLayout mTransitionPlayerStateSection;
    private RadioGroup mPlayerStateGroup;
    private TextView mExtraMessageDataLabel;
    private EditText mExtraMessageDataEditText;
    private Button mCancelButton;
    private Button mOkButton;

    public static PlayerTransitionDialogFragment newInstance(String playerId,
            boolean isTransition) {
        PlayerTransitionDialogFragment instance = new PlayerTransitionDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PLAYER_ID, playerId);
        bundle.putBoolean(KEY_IS_TRANSITION, isTransition);
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onOkPressed();
            return true;
        }
        return false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.player_transition_dialog, null);
        mTransitionPlayerStateSection = (LinearLayout) view.findViewById(
                R.id.transition_player_state_section);
        mPlayerStateGroup = (RadioGroup) view.findViewById(R.id.radio_player_states);
        mExtraMessageDataLabel = (TextView) view.findViewById(R.id.extra_message_data_label);
        mExtraMessageDataEditText = (EditText) view.findViewById(R.id.extra_message_data);
        mCancelButton = (Button) view.findViewById(R.id.button_cancel);
        mOkButton = (Button) view.findViewById(R.id.button_ok);

        // Find the valid player states.
        GameManagerClient client = GameDebuggerApplication.getInstance().getCastConnectionManager()
                .getGameManagerClient();
        GameManagerState state = client.getCurrentState();
        PlayerInfo player = state.getPlayer(getArguments().getString(KEY_PLAYER_ID));
        if (player == null) {
            return null;
        }
        if (player.isConnected()) {
            boolean defaultSelectionSet = false;
            for (int i = 0; i < PLAYER_STATES.length; i++) {
                if (PLAYER_STATES[i] != player.getPlayerState()) {
                    RadioButton radioButton = new RadioButton(getActivity());
                    radioButton.setText(getText(PLAYER_STATE_RESOURCE_IDS[i]));
                    radioButton.setId(PLAYER_STATES[i]);
                    radioButton.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    mPlayerStateGroup.addView(radioButton);

                    // Select the first element.
                    if (!defaultSelectionSet) {
                        mPlayerStateGroup.check(PLAYER_STATES[i]);
                        defaultSelectionSet = true;
                    }
                }
            }
        } else {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(getText(R.string.player_state_available));
            radioButton.setId(GameManagerClient.PLAYER_STATE_AVAILABLE);
            radioButton.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            mPlayerStateGroup.addView(radioButton);
            mPlayerStateGroup.check(GameManagerClient.PLAYER_STATE_AVAILABLE);
        }

        if (!getArguments().getBoolean(KEY_IS_TRANSITION)) {
            mTransitionPlayerStateSection.setVisibility(View.GONE);
            mExtraMessageDataLabel.setText(R.string.game_message_label);
            mExtraMessageDataEditText.setMinLines(4);
            mOkButton.setText(getText(R.string.button_send_game_message));

        }
        builder.setView(view);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkPressed();
            }
        });

        return builder.create();
    }

    public void onOkPressed() {
        JSONObject extraMessageData = null;
        if (mExtraMessageDataEditText.getText() != null
                && mExtraMessageDataEditText.getText().length() > 0) {
            try {
                extraMessageData = new JSONObject(
                        mExtraMessageDataEditText.getText().toString());
            } catch (JSONException e) {
                mExtraMessageDataEditText.setError(getText(R.string.error_not_valid_json));
                return;
            }
        }
        GameManagerClient client = GameDebuggerApplication.getInstance().getCastConnectionManager()
                .getGameManagerClient();
        String playerId = getArguments().getString(KEY_PLAYER_ID);

        if (!getArguments().getBoolean(KEY_IS_TRANSITION)) {
            client.sendGameMessage(playerId, extraMessageData);
        } else {
            PendingResult<GameManagerClient.GameManagerResult> result;
            int targetState = mPlayerStateGroup.getCheckedRadioButtonId();
            if (targetState == GameManagerClient.PLAYER_STATE_AVAILABLE) {
                GameDebuggerApplication.getInstance().setRequestInProgress(true);
                result = client.sendPlayerAvailableRequest(playerId, extraMessageData);
            } else if (targetState == GameManagerClient.PLAYER_STATE_READY) {
                GameDebuggerApplication.getInstance().setRequestInProgress(true);
                result = client.sendPlayerReadyRequest(playerId, extraMessageData);
            } else if (targetState == GameManagerClient.PLAYER_STATE_IDLE) {
                GameDebuggerApplication.getInstance().setRequestInProgress(true);
                result = client.sendPlayerIdleRequest(playerId, extraMessageData);
            } else if (targetState == GameManagerClient.PLAYER_STATE_PLAYING) {
                GameDebuggerApplication.getInstance().setRequestInProgress(true);
                result = client.sendPlayerPlayingRequest(playerId, extraMessageData);
            } else if (targetState == GameManagerClient.PLAYER_STATE_QUIT) {
                GameDebuggerApplication.getInstance().setRequestInProgress(true);
                result = client.sendPlayerQuitRequest(playerId, extraMessageData);
            } else {
                Log.w(TAG, "No target state for button.");
                return;
            }
            result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
                @Override
                public void onResult(GameManagerClient.GameManagerResult result) {
                    GameDebuggerApplication.getInstance().setRequestInProgress(false);
                }
            });
        }
        dismiss();
    }
}