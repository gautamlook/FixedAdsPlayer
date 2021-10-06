package com.look.fixedadsloader;

import static com.google.android.exoplayer2.C.TIME_END_OF_SOURCE;
import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.mp4.Track;
import com.google.android.exoplayer2.source.ads.AdPlaybackState;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FixedAdsLoader implements Player.EventListener, AdsLoader {
    private Player player = null;
    @Nullable
    private EventListener eventListener;
    public final static String FIXED_ADS_TAG = "https://fixedadsurl";
    private AdPlaybackState adPlaybackState;
    private List<String> supportedMimeTypes = ImmutableList.of();
    private boolean adsMid = false;
    private boolean adsEnd = false;
    private String start = "";
    private String midAds = "";
    private String endAds = "";
    private Context context;
    private int midPosition = 0;

    public FixedAdsLoader(Context context) {
        this.context = checkNotNull(context).getApplicationContext();
        this.adPlaybackState = new AdPlaybackState(0, 0);
    }

    @Override
    public void setPlayer(@Nullable Player player) {
        this.player = player;
    }

    @Override
    public void release() {
        player.removeListener(this);
        player = null;
        eventListener = null;
        adPlaybackState = null;
    }

    @Override
    public void setSupportedContentTypes(int... contentTypes) {
        List<String> supportedMimeTypes = new ArrayList<>();
        for (@C.ContentType int contentType : contentTypes) {
            // IMA does not support Smooth Streaming ad media.
            if (contentType == C.TYPE_DASH) {
                supportedMimeTypes.add(MimeTypes.APPLICATION_MPD);
            } else if (contentType == C.TYPE_HLS) {
                supportedMimeTypes.add(MimeTypes.APPLICATION_M3U8);
            } else if (contentType == C.TYPE_OTHER) {
                supportedMimeTypes.addAll(
                        Arrays.asList(
                                MimeTypes.VIDEO_MP4,
                                MimeTypes.VIDEO_WEBM,
                                MimeTypes.VIDEO_H263,
                                MimeTypes.AUDIO_MP4,
                                MimeTypes.AUDIO_MPEG));
            }
        }
        this.supportedMimeTypes = Collections.unmodifiableList(supportedMimeTypes);
    }

    @Override
    public void start(AdsMediaSource adsMediaSource, DataSpec adTagDataSpec, Object adsId, AdViewProvider adViewProvider, EventListener eventListener) {
        Assertions.checkState(
                player != null, "Set player using adsLoader.setPlayer before preparing the player."
        );
        this.eventListener = eventListener;
        player.addListener(this);
        adPlaybackState = adPlaybackState.withAdCount(/* adGroupIndex= */ 0, /* adCount= */ 1);
        if (!start.isEmpty()) {
            adPlaybackState = adPlaybackState.withAdUri(
                    0,
                    0,
                    Uri.parse(start)
            );
            updateAdPlaybackState();
        } else if (!midAds.isEmpty()) {
            adPlaybackState = adPlaybackState.withSkippedAdGroup(0);
            updateAdPlaybackState();
        }
        if (start.isEmpty() && midAds.isEmpty() && !endAds.isEmpty()) {
            adPlaybackState = new AdPlaybackState(0, TIME_END_OF_SOURCE);
            adPlaybackState = adPlaybackState.withAdCount(/* adGroupIndex= */ 0, /* adCount= */ 1);
            adPlaybackState = adPlaybackState.withAdUri(
                    0,
                    0,
                    Uri.parse(endAds)
            );
            eventListener.onAdPlaybackState(adPlaybackState);
            adsEnd = true;
        }
        if(start.isEmpty() && midAds.isEmpty() && endAds.isEmpty()){
            adPlaybackState = adPlaybackState.withSkippedAdGroup(0);
            assert eventListener != null;
            eventListener.onAdPlaybackState(adPlaybackState);
        }

    }

    private void updateAdPlaybackState() {
        eventListener.onAdPlaybackState(adPlaybackState);
    }

    @Override
    public void stop(AdsMediaSource adsMediaSource, EventListener eventListener) {
        if (player == null)
            return;

    }

    @Override
    public void handlePrepareComplete(AdsMediaSource adsMediaSource, int adGroupIndex, int adIndexInAdGroup) {
    }

    @Override
    public void handlePrepareError(AdsMediaSource adsMediaSource, int adGroupIndex, int adIndexInAdGroup, IOException exception) {
        if (player == null)
            return;
        adPlaybackState = adPlaybackState.withSkippedAdGroup(adGroupIndex);
        assert eventListener != null;
        eventListener.onAdPlaybackState(adPlaybackState);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Player.TimelineChangeReason int reason) {
        if (timeline.isEmpty()) {
            // The player is being reset or contains no media.
            return;
        }
    }

    @Override
    public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
        Log.e("WER", +reason + " onPositionDiscontinuity");

        if (reason == 3 && !player.isPlayingAd()) {
            adPlaybackState = adPlaybackState.withSkippedAdGroup(0);
            assert eventListener != null;
            eventListener.onAdPlaybackState(adPlaybackState);
            if (!adsMid && !midAds.isEmpty()) {
                if (midPosition == 0)
                    midPosition = (int) (player.getDuration() / 2 * 1000);
                else midPosition = (int) (midPosition * C.MICROS_PER_SECOND);
                adPlaybackState = new AdPlaybackState(0, midPosition);
                adPlaybackState = adPlaybackState.withAdCount(/* adGroupIndex= */ 0, /* adCount= */ 1);
                adPlaybackState = adPlaybackState.withAdUri(
                        0,
                        0,
                        Uri.parse(midAds)
                );
                eventListener.onAdPlaybackState(adPlaybackState);
                adsMid = true;
            } else if (!adsEnd && !endAds.isEmpty()) {
                adPlaybackState = new AdPlaybackState(0, TIME_END_OF_SOURCE);
                adPlaybackState = adPlaybackState.withAdCount(/* adGroupIndex= */ 0, /* adCount= */ 1);
                adPlaybackState = adPlaybackState.withAdUri(
                        0,
                        0,
                        Uri.parse(endAds)
                );
                eventListener.onAdPlaybackState(adPlaybackState);
                adsEnd = true;
            }
        } else if (reason == 11) {
            if (midPosition == 0)
                midPosition = (int) (player.getDuration() / 2 * 1000);
            else midPosition = (int) (midPosition * C.MICROS_PER_SECOND);
            adPlaybackState = new AdPlaybackState(0, midPosition);
            adPlaybackState = adPlaybackState.withAdCount(/* adGroupIndex= */ 0, /* adCount= */ 1);
            adPlaybackState = adPlaybackState.withAdUri(
                    0,
                    0,
                    Uri.parse(midAds)
            );
            eventListener.onAdPlaybackState(adPlaybackState);
            adsMid = true;
        }
    }

    public void skipAds() {
        if (player.isPlayingAd()) {
            adPlaybackState = adPlaybackState.withSkippedAdGroup(0);
            assert eventListener != null;
            eventListener.onAdPlaybackState(adPlaybackState);
        }
    }

    public void startAdsUrl(String startAdsUrl) {
        this.start = startAdsUrl;
    }

    public void endAdsUrl(String endAdsUrl) {
        this.endAds = endAdsUrl;
    }

    public void midAdsUrl(String midAdsUrl, int midPosition) {
        this.midAds = midAdsUrl;
        this.midPosition = midPosition;
    }
}
