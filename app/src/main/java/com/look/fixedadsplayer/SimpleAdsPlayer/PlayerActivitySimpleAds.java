/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.look.fixedadsplayer.SimpleAdsPlayer;

import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.look.fixedadsplayer.BaseFragmentActivity;
import com.look.fixedadsplayer.R;
import com.look.fixedadsplayer.lib.PlayerActivityContract;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public final class PlayerActivitySimpleAds extends BaseFragmentActivity implements PlayerControlView.VisibilityListener, PlayerActivityContract.PlayerActivityView {
    public static DefaultTrackSelector trackSelector = null;
    private static final String TAG = "PlayerActivity";
    private final int FAST_FORWARD_MESSAGE = 1, REWIND_MESSAGE = 0;
    private boolean isPaused = false;
    private PlayerView playerView;
    private ProgressBar pbExoPlayer;
    private ImageView ivAppLogo;
    private LinearLayout llControlView, llNextVideoOverlayInfo;
    private PlayerManagerSimpleAds player;
    //    private DialogsUtil dialogsUtil;
    RelativeLayout top_rel;
    private boolean handlePlayPauseButton = false;
    private String startAds = "", midAds = "",endAds="";
    private ImageButton playPauseButton;
    private long videoCurrentDurationToStore = 0;
    private Button selectTracksButton;
    private boolean errorFlag = false;
    //    private PlayerPresenterImpl presenter;
    private boolean isCounterIncremented = false;
    private String feedUrl = "";
    private String duration = "", title;
    private boolean isVideoPlayCalled = false, isBufferingCalled = false;
    private Button setting;
    private long introStartDuration = 0;
    private long introEndDuration = 0;
    private long introEndCredits = 0;
    private TextView videoQuility, tvNextVideoOverlayMsg, tvMediaTitle, tvSubTitle, nextTitle;
    private LinearLayout skipCreditLayout, topLayout;
    private ImageView nextThumbnail;
    private TextView adsTitle;
    private RelativeLayout skipAds;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plyra_player_activity);
        tvNextVideoOverlayMsg = findViewById(R.id.tv_next_video_overlay_msg);
        llNextVideoOverlayInfo = findViewById(R.id.ll_next_video_overlay_info);
        setting = findViewById(R.id.setting);
        adsTitle =findViewById(R.id.adsTitle);
        skipAds =findViewById(R.id.skipAds);
        setting.setVisibility(View.GONE);
        videoQuility = findViewById(R.id.videoQuility);
        top_rel = findViewById(R.id.top);
        feedUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4";//getIntent().getExtras().getString("CALL_FROM_ACTIVITY");
        title = "";//getIntent().getExtras().getString("TITLE");
        startAds = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";//getIntent().getExtras().getString(VAST_URL);
        midAds = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";//getIntent().getExtras().getString(VAST_URL);
        endAds = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";//getIntent().getExtras().getString(VAST_URL);
        //midAds = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";//getIntent().getExtras().getString(VAST_URL);
        //TextView topTitle =findViewById(R.id.topTitle);
        // topTitle.setText(title);
        try {
            feedUrl = feedUrl.trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        findViewById();
        fetchData();


        try {
            /*
             **This is used for enable CC Option only once for each app launch */
          //  if (MainActivity.trackSelectionFactory == null) {
              //  MainActivity.trackSelectionFactory = new AdaptiveTrackSelection.Factory();
                // Create a player instance.
                trackSelector = new DefaultTrackSelector();
                trackSelector.setParameters(new DefaultTrackSelector.ParametersBuilder().build());
           // }

            playerView.setControllerVisibilityListener(this);

            if (player != null)
                player.release();
            player = new PlayerManagerSimpleAds(this, startAds,
                    this, setting, playerView,midAds,adsTitle,skipAds,endAds);
            //new Handler().postDelayed(this::skipbuttonCheck, 5000 * 2);
        } catch (Exception e) {
            e.printStackTrace();
            onError();
        }
    }


    @Override
    public void onVideoPause() {
        handlePlayPauseButton = false;
        playPauseButton.setVisibility(View.VISIBLE);
        playPauseButton.setImageResource(R.drawable.play_icon_selector);
    }

    @Override
    public void onVideoPlay() {
        handlePlayPauseButton = true;
        playPauseButton.setVisibility(View.VISIBLE);
        playPauseButton.setImageResource(R.drawable.pause_icon_selector);
    }

    @Override
    public void onVisibilityChange(int visibility) {
    }

    private void findViewById() {
        playerView = findViewById(R.id.player_view);
        pbExoPlayer = findViewById(R.id.pb_exo_player);
        llControlView = findViewById(R.id.controls_root);
        selectTracksButton = findViewById(R.id.select_tracks_button);
        llNextVideoOverlayInfo = findViewById(R.id.ll_next_video_overlay_info);
        playPauseButton = findViewById(R.id.playpause);
        playPauseButton.setOnClickListener(view -> {
            if (!handlePlayPauseButton && player != null) {
                player.play();
            } else if (player != null)
                player.pause();
        });

        keyListener(playPauseButton);
        setting.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getKeyCode() == KEYCODE_DPAD_DOWN) {
                playPauseButton.setFocusable(true);
                playPauseButton.requestFocus();
                return true;
            }
            return false;
        });

        setting.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    videoQuility.setVisibility(View.VISIBLE);
                else videoQuility.setVisibility(View.INVISIBLE);
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        if (player != null)
            player.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            isPaused = false;
            player.reset();

        }
    }

    @Override
    public void adCompleted() {
//        LogUtil.getInstance().i(TAG, "adCompleted: ");
    }

    @Override
    public void onDestroy() {


        if (player != null)
            player.release();
        if (handler != null)
            handler.removeCallbacks(null);

        super.onDestroy();
    }

    public void keyListener(ImageButton imageButton) {
        imageButton.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getKeyCode() == KEYCODE_DPAD_DOWN) {
                if (!flagDow)
                    return true;
                findViewById(R.id.exo_progress).setFocusable(true);
                findViewById(R.id.exo_progress).requestFocus();
                return true;
            }
            return false;
        });
        ((DefaultTimeBar) findViewById(R.id.exo_progress)).setScrubberColor(ContextCompat.getColor(getApplication(), R.color.black));
        findViewById(R.id.exo_progress).setOnFocusChangeListener((view, b) -> {
            if (b) {
                ((DefaultTimeBar) findViewById(R.id.exo_progress)).setScrubberColor(ContextCompat.getColor(getApplication(), R.color.white));
            } else
                ((DefaultTimeBar) findViewById(R.id.exo_progress)).setScrubberColor(ContextCompat.getColor(getApplication(), R.color.black));
        });
    }

    private void fetchData() {

    }

    @Override
    public void adStart(long playerTotalDurationInSecond) {

//        LogUtil.getInstance().i(TAG, "adStart: " + playerTotalDurationInSecond);
    }

    @Override
    public void onEverySecond(boolean b) {
        if (introEndCredits > 0) {
            long startDurationSkip = introEndCredits;
            long hideDurationSkip = player.getVideoDuration() / 1000 - 5;
            if (player.getVideoCurrentPosition() / 1000 > startDurationSkip && player.getVideoCurrentPosition() / 1000 < hideDurationSkip) {
                onShowEndSkipButton();
            } else {
                onHideEndSkipButton();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        setUpFeed();


    }

    public void setUpFeed() {
        try {
            if (player != null) {
                if (!isPaused) {

                    player.init(feedUrl);
                    player.play();
                    player.seekTo(videoCurrentDurationToStore);
                    isPaused = false;
                    errorFlag = true;
                } else {
                    player.play();
                    isPaused = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError();
        }
    }


    /**
     * Player States onBuffering
     */
    @Override
    public void onBuffering() {
        pbExoPlayer.setVisibility(View.VISIBLE);
        top_rel.setVisibility(View.GONE);
        playPauseButton.setVisibility(View.GONE);
        if (!isBufferingCalled) {

            isBufferingCalled = true;
        }
    }

    @Override
    public void onReady(boolean isPlaying, long playerTotalDurationInSecond) {
        if (isPlaying) {
            top_rel.setVisibility(View.VISIBLE);
            playPauseButton.setVisibility(View.VISIBLE);

        }
//        if (SharedPreferenceUtils.getInstance(this).getIntValue("PUB1", -1) != -1 && SharedPreferenceUtils.getInstance(this).getStringValue("PUB2", "T").equalsIgnoreCase("R"))
//            callVideoQuility();
//        SharedPreferenceUtils.getInstance(this).setValue("PUB2", "T");
        playPauseButton.setVisibility(View.VISIBLE);
        pbExoPlayer.setVisibility(View.GONE);
        playPauseButton.setSelected(true);
        playPauseButton.requestFocus();
        flagDow = true;
        if (!player.isAdPlaying()) {
            if (isPlaying) {
                isBufferingCalled = false;

                if (isVideoPlayCalled) {
                    Log.i(TAG, "onReady: ");
                } else {
                    isVideoPlayCalled = true;
                }
            }
        }

    }

    @Override
    public void onShowEndSkipButton() {


    }

    @Override
    public void onHideEndSkipButton() {
        skipCreditLayout.setVisibility(View.GONE);

    }

    @Override
    public void onShowNextVideoOverlay() {

    }

    @Override
    public void onHideNextVideoOverlay() {
//        llNextVideoOverlayInfo.setVisibility(View.GONE);
    }

    @Override
    public void onError() {

        /*calculation to display Retry Next dialog*/

        if (!errorFlag)
            return;

        if (player != null)
            player.pause();

//        new DialogsUtil(this).showDialogWithTwoButton(this, 0, errorMessage);

    }

    int listStartPosition = 0;

    @Override
    public void onCompleted() {

        playerView.setVisibility(View.GONE);
        finishMyActivity();

    }

    @Override
    public void playerEverySecondLive(int playerDurationInSecond) {

    }


    private void finishMyActivity() {
        if (player != null) {
            player.release();
            player = null;
        }
        PlayerActivitySimpleAds.this.finish();
    }

    private Handler handler = new Handler();
    boolean flagDow = false;

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        FrameLayout rt = findViewById(R.id.rootBr);
        if (rt.getChildCount() > 0) {
            if (playerView.isControllerVisible())
                playerView.hideController();
            return super.dispatchKeyEvent(event) || playerView.dispatchMediaKeyEvent(event);
        }

        //For disable Y settingPageButton of Game controller
        boolean result = false;

        if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y ||
                (player != null && player.isAdPlaying() &&
                        event.getKeyCode() != KeyEvent.KEYCODE_BACK)) {
            player.skipAdsFromView();
            return super.dispatchKeyEvent(event) || playerView.dispatchMediaKeyEvent(event);
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (player != null && player.getPlayer() != null && !player.isAdPlaying()) {

                switch (event.getKeyCode()) {

                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:

//                        result = true;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:

                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:

                        break;
                    case KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_MEDIA_REWIND:

                        if (!player.isAdPlaying() && !playerView.isControllerVisible()) {
                            result = true;
                            playerView.showController();
                            flagDow = true;
                            playPauseButton.setFocusable(true);
                            playPauseButton.requestFocus();
                        }
                        break;
                    case KeyEvent.KEYCODE_BUTTON_L1:
//                        rewindCounterCalculation();
//                        forwardAndBackwardTimer(false);
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (!player.isAdPlaying() && !playerView.isControllerVisible()) {
                            result = true;
                            playerView.showController();
                            flagDow = true;
                            playPauseButton.setFocusable(true);
                            playPauseButton.requestFocus();
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (!player.isAdPlaying() && !playerView.isControllerVisible()) {
                            playerView.showController();
                            flagDow = false;
                            playPauseButton.setFocusable(true);
                            playPauseButton.requestFocus();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    flagDow = true;
                                }
                            }, 500);
                        }
                        return true;
//                        if (playerView.isControllerVisible())
//                            return true;
//                        else {
//                            if (!player.isAdPlaying() && !playerView.isControllerVisible()) {
//                                result = true;
//                                playerView.showController();
//                                playPauseButton.setFocusable(true);
//                                playPauseButton.requestFocus();
//                            }
//                            return true;
//                        }
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                        if (!player.isAdPlaying() && !playerView.isControllerVisible()) {
                            result = true;
                            flagDow = true;
                            playerView.showController();
                            playPauseButton.setFocusable(true);
                            playPauseButton.requestFocus();
                        }
                        break;
                    case KeyEvent.KEYCODE_BUTTON_R1:
//                        forwardCounterCalculation();
//                        forwardAndBackwardTimer(true);
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:

                        if (!player.isAdPlaying() && !playerView.isControllerVisible()) {
                            result = true;
                            playerView.showController();
                            flagDow = true;
                            playPauseButton.setFocusable(true);
                            playPauseButton.requestFocus();

                        }

//                        if (forwardAndBackwardTimer != null) {
//                            forwardCounter = 0;
//                            rewindCounter = 0;
//                            forwardAndBackwardTimer.cancel();
//                            forwardAndBackwardTimer = null;
//                        }

                        break;
                    case KeyEvent.KEYCODE_MENU:
                        if (!player.isAdPlaying()) {
                            result = true;
                            playerView.showController();
                        }

                        break;
                    default:
                        break;

                }

            }
        }
        return result || super.dispatchKeyEvent(event) || playerView.dispatchMediaKeyEvent(event);
    }


    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return playerView.dispatchGenericMotionEvent(event) || super.onGenericMotionEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                if (player != null)
                    player.release();
                player = new PlayerManagerSimpleAds(this, startAds,
                        this, selectTracksButton, playerView, midAds, adsTitle, skipAds, endAds);
                player.init(feedUrl);
            } else {
                finishMyActivity();
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError();
        }
    }

    @Override
    public void onBackPressed() {

        FrameLayout rt = findViewById(R.id.rootBr);
        if (rt.getChildCount() > 0) {
            rt.removeAllViews();
            rt.setVisibility(View.GONE);
            return;
        }
        if (playerView.isControllerVisible()) {
            playerView.hideController();
            return;
        }
        finishMyActivity();


    }

    private boolean startMarkerFlag = true;

    @Override
    public void playerEverySecondVOD(int playerCurrentDurationInSecond) {

    }


    @Override
    public void onAdEvent(AdEvent adEvent) {
//        LogUtil.getInstance().i(TAG, "onAdEvent: " + adEvent.getType().name());
        switch (adEvent.getType()) {
            case AD_PROGRESS:
                // Do nothing or else log will be filled by these messages.
                break;
            case CONTENT_RESUME_REQUESTED:
                break;
            case ALL_ADS_COMPLETED:
                try {

                    isVideoPlayCalled = true;


                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case STARTED:
            case SKIPPED:
                break;
        }
    }


    public void callVideoQuility() {
//        HashMap<Integer, Format> Quility = new HashMap<>();
//        for (int i = 0; i < trackSelector.getCurrentMappedTrackInfo().getRendererCount(); i++) {
//            int trackType = trackSelector.getCurrentMappedTrackInfo().getRendererType(/* rendererIndex= */ i);
//            if (trackType == 2) {
//                TrackGroupArray trackGroupArray = trackSelector.getCurrentMappedTrackInfo().getTrackGroups(i);
//                for (int j = 0; j < trackGroupArray.get(0).length; j++) {
//                    int width = trackGroupArray.get(0).getFormat(j).width;
//                    int height = trackGroupArray.get(0).getFormat(j).height;
//                    int bitrate = trackGroupArray.get(0).getFormat(j).bitrate;
//                    Log.e("VIDEOSQ= ", "" + width + "X" + height);
//                    Quility.put(width, trackGroupArray.get(0).getFormat(j));
//                }
//                break;
//            }
//        }
//        List<Map.Entry<Integer, Format>> list = sortByComparator(Quility, false);
//        GuidedAction action = new GuidedAction.Builder().build();
//        action.setId(SharedPreferenceUtils.getInstance(this).getIntValue("PUB1", -1));
//        onGuidedActionClicked(action, list);
    }

    private List<Map.Entry<Integer, Format>> sortByComparator(Map<Integer, Format> unsortMap, final boolean order) {

        List<Map.Entry<Integer, Format>> list = new LinkedList<Map.Entry<Integer, Format>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<Integer, Format>>() {
            public int compare(Map.Entry<Integer, Format> o1,
                               Map.Entry<Integer, Format> o2) {
                if (order) {
                    return o1.getKey().compareTo(o2.getKey());
                } else {
                    return o2.getKey().compareTo(o1.getKey());

                }
            }
        });
        if (list.size() > 2)
            list = list.subList(0, 3);
        return list;
    }

}