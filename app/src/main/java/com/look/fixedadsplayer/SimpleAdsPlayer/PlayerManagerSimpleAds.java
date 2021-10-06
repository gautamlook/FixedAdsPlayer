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


import static com.look.fixedadsloader.FixedAdsLoader.FIXED_ADS_TAG;
import static com.look.fixedadsplayer.SimpleAdsPlayer.PlayerActivitySimpleAds.trackSelector;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.look.fixedadsloader.FixedAdsLoader;
import com.look.fixedadsplayer.R;
import com.look.fixedadsplayer.lib.AppConst;
import com.look.fixedadsplayer.lib.MyPlayerEvent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


/**
 * Manages the {@link ExoPlayer}, the IMA plugin and all video playback.
 */
/* package */
public final class PlayerManagerSimpleAds implements
        Player.EventListener, View.OnClickListener {
    private static final String TAG = "Player Manager";
    private boolean isAdPlaying = false;
    private final DataSource.Factory dataSourceFactory;

    private SimpleExoPlayer player;
    private long contentPosition;
    private MyPlayerEvent myPlayerEvent;

    private boolean startAutoPlay;
    private int startWindow = 0;
    private long startPosition = 0;

    //    private TrackGroupArray lastSeenTrackGroupArray;
    private FragmentActivity activity;
    private Button selectTracksButton;
    private boolean isShowingTrackSelectionDialog;

    private PlayerView playerView;


    private TimerTask timerTask;
    private Timer timer;

    private int liveCurrentPlayDurationCounter = 0;
    /**
     * Handle Thread
     **/
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (player != null && msg.what == AppConst.MSG_PLAYTIME) {
                if (player.isCurrentWindowDynamic()) {
                    try {
                        if (player.getPlayWhenReady())
                            liveCurrentPlayDurationCounter++;

                        if (myPlayerEvent != null)
                            myPlayerEvent.playerEverySecondLive(liveCurrentPlayDurationCounter);
                        if (player.isPlayingAd())
                            setAdsValue();
                        else skipAds.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    myPlayerEvent.onEverySecond(false);
                    try {
                        /*For VOD Media Play*/
                        if (player != null && !player.isCurrentWindowDynamic()) {
                            int startDuration = (int) (player.getDuration() / 1000) - 15;
                            int hideDuration = startDuration + 5;
                            if (player.getCurrentPosition() / 1000 > startDuration && player.getCurrentPosition() / 1000 < hideDuration) {
                                if (myPlayerEvent != null)
                                    myPlayerEvent.onShowNextVideoOverlay();
                            } else {
                                if (myPlayerEvent != null)
                                    myPlayerEvent.onHideNextVideoOverlay();
                            }
                            if (myPlayerEvent != null)
                                myPlayerEvent.playerEverySecondVOD((int) (player.getCurrentPosition() / 1000));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (player.isPlayingAd())
                        setAdsValue();
                    else skipAds.setVisibility(View.GONE);
                }
            }
            return true;
        }
    });

    private long getMilliToSecond(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        return seconds;
    }

    private String millisecondsToTime(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        String secondsStr = Long.toString(seconds);
        String secs;
        if (secondsStr.length() >= 2) {
            secs = secondsStr.substring(0, 2);
        } else {
            secs = "0" + secondsStr;
        }

        return minutes + ":" + secs;
    }

    int countTime = 0;

    private void setAdsValue() {
        if (player != null && adsTitle != null && player.getDuration() > 0) {
            adsTitle.setText("Ad:(" + millisecondsToTime(player.getDuration() - player.getCurrentPosition()) + ")");
            long time = 6 - TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition());
            if (time < 6 && time > 0) {
                if (TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) >= 30)
                    skipAds.setVisibility(View.VISIBLE);
                else skipAds.setVisibility(View.GONE);
                ((TextView) skipAds.getChildAt(0)).setText(time + "");
            } else {
                ((TextView) skipAds.getChildAt(0)).setText("Skip Ad");
            }


        }
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private String start, midUrl, endUrl;
    private FixedAdsLoader fixedAdsLoader;
    private TextView adsTitle;
    private RelativeLayout skipAds;

    public PlayerManagerSimpleAds(FragmentActivity activity, String start, MyPlayerEvent myPlayerEvent,
                                  Button selectTracksButton, PlayerView playerView, String midAds, TextView adsTitle, RelativeLayout skipAds, String endAds) {
        this.myPlayerEvent = myPlayerEvent;
        this.fixedAdsLoader = new FixedAdsLoader(activity);
        this.activity = activity;
        this.selectTracksButton = selectTracksButton;
        this.playerView = playerView;
        this.adsTitle = adsTitle;
        this.skipAds = skipAds;
        this.dataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(activity, activity.getString(R.string.app_name)),
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                /* allowCrossProtocolRedirects= */ true);

        startAutoPlay = true;
        this.selectTracksButton.setOnClickListener(this);
        this.start = start;
        this.midUrl = midAds;
        this.endUrl = endAds;

    }

    private void cancelTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
            timer = null;
        }
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    public long getVideoDuration() {
        if (player != null)
            return player.getDuration();
        else return 0;
    }

    public long getVideoCurrentPosition() {
        if (player != null)
            return player.getCurrentPosition();
        else return 0;
    }


    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
        cancelTask();
    }

    /**
     * Clear previous state
     */
    public void resetPlayerState() {
        startWindow = 0;
        startPosition = 0;
        cancelTask();
    }

    private void updateButtonVisibilities() {
        if (player == null)
            return;
        if (player.isPlayingAd()) {
            adsTitle.setVisibility(View.VISIBLE);
        } else {
            adsTitle.setVisibility(View.GONE);
            skipAds.setVisibility(View.GONE);
        }
    }

    public String getUrlHolder() {
        return urlHolder;
    }

    // User controls
    private String urlHolder = "";

    public void init(String contentUrl) {
        this.urlHolder = contentUrl;
        MediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(dataSourceFactory)
                        .setAdsLoaderProvider(unusedAdTagUri -> fixedAdsLoader)
                        .setAdViewProvider(playerView);
        player = new SimpleExoPlayer.Builder(activity.getApplication()).setMediaSourceFactory(mediaSourceFactory).build();
        Uri contentUri = Uri.parse(contentUrl);
        Uri adTagUri = Uri.parse(FIXED_ADS_TAG);
        fixedAdsLoader.startAdsUrl(start);
        fixedAdsLoader.midAdsUrl(midUrl, 0);
        fixedAdsLoader.endAdsUrl(endUrl);
        MediaItem mediaItem = new MediaItem.Builder().setUri(contentUri).setAdTagUri(adTagUri).build();

        fixedAdsLoader.setPlayer(player);
        playerView.setPlayer(player);
        player.seekTo(contentPosition);
        player.addMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(startAutoPlay);
        player.addListener(this);
        /*Handle Player Events*/
        player.addListener(new PlayerEventListener(contentUrl));
        player.addAnalyticsListener(new EventLogger(trackSelector));
        updateButtonVisibilities();
        liveCurrentPlayDurationCounter = 0;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                /*Use to run code in UI Thread*/
                playerView.post(() -> handler.sendEmptyMessage(AppConst.MSG_PLAYTIME));
            }
        };
        timer.schedule(timerTask, 0, 999);
    }

    public boolean isAdPlaying() {
        return player.isPlayingAd();
    }

    public boolean isCurrentWindowDynamic() {
        return player != null && player.isCurrentWindowDynamic();
    }

    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    public void reset() {
        if (player != null) {
            if (!isAdPlaying)
                contentPosition = player.getContentPosition();
            player.release();
            player = null;
            cancelTask();

//            if (adsLoader != null)
//                adsLoader.detachPlayer();
        }
    }

    public void play() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
        cancelTask();
    }


    private MediaSource buildMediaSource(Uri uri) {
        @ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                if (uri.getLastPathSegment() != null && uri.getLastPathSegment().contains(".m3u8"))
                    return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                else
                    return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    // Internal methods.

    @Override
    public void onClick(View view) {


    }

    public void skipAdsFromView() {
        if (((TextView) skipAds.getChildAt(0)).getText().toString().equals("Skip Ad")) {
            fixedAdsLoader.skipAds();
        }
    }

    /**
     * Handle Player Events
     */
    private class PlayerEventListener implements Player.EventListener {
        private String contentUrl;

        PlayerEventListener(String contentUrl) {
            this.contentUrl = contentUrl;
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    if (myPlayerEvent != null)
                        myPlayerEvent.onBuffering();
                    break;
                case Player.STATE_ENDED:
                    if (myPlayerEvent != null)
                        myPlayerEvent.onCompleted();
                    break;
                case Player.STATE_READY:
                    if (myPlayerEvent != null)
                        myPlayerEvent.onReady(playWhenReady, player.getDuration() / 1000);
                    if (start.isEmpty() && !midUrl.isEmpty())
                        midAdsCall();
                    break;
            }
            updateStartPosition();
            updateButtonVisibilities();
        }

        private boolean flag = false;

        @SuppressLint("WrongConstant")
        private void midAdsCall() {
            if (!flag)
                fixedAdsLoader.onPositionDiscontinuity(11);
            flag = true;
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (player != null && player.getPlaybackError() != null) {
                // The user has performed a seek whilst in the error state. Update the resume position so
                // that if the user then retries, playback resumes from the position to which they seeked.
                updateStartPosition();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (isBehindLiveWindow(error)) {
                clearStartPosition();
                init(contentUrl);
            } else {
                updateStartPosition();
                updateButtonVisibilities();
                if (myPlayerEvent != null)
                    myPlayerEvent.onError();
                cancelTask();
                switch (error.type) {
                    case ExoPlaybackException.TYPE_RENDERER:

                        break;
                    case ExoPlaybackException.TYPE_SOURCE:

                        break;
                    case ExoPlaybackException.TYPE_UNEXPECTED:

                        break;
                    default:
                        break;
                    case ExoPlaybackException.TYPE_REMOTE:
                        break;
                }
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            updateButtonVisibilities();

        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            Log.e("MEDIAID", "" + mediaItem.mediaId);
        }

        @Override
        public void onIsLoadingChanged(boolean isLoading) {

        }
    }

    public void seekTo(long contentPosition) {
        if (player != null) {
            player.seekTo(contentPosition);
        }
        // fixedAdsLoader.removeAds();
    }

    private String contentUrl;

//    PlayerEventListener(String contentUrl) {
//        this.contentUrl = contentUrl;
//    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if (isPlaying) {
            myPlayerEvent.onVideoPlay();
        } else {
            myPlayerEvent.onVideoPause();
        }
    }

}
