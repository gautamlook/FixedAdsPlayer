package com.look.fixedadsplayer.lib;

import android.util.Log;

import com.google.android.exoplayer2.source.ads.AdPlaybackState;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.upstream.DataSpec;

public class MyAdLister implements AdsLoader.EventListener {
    private static final String TAG = "MyAdLister";

    @Override
    public void onAdPlaybackState(AdPlaybackState adPlaybackState) {
        Log.i(TAG, "onAdPlaybackState: " + adPlaybackState);

    }

    @Override
    public void onAdLoadError(AdsMediaSource.AdLoadException error, DataSpec dataSpec) {
        Log.i(TAG, "onAdLoadError: " + error.type);
    }

    @Override
    public void onAdClicked() {
        Log.i(TAG, "onAdClicked: ");
    }

    @Override
    public void onAdTapped() {
        Log.i(TAG, "onAdTapped: ");
    }
}
