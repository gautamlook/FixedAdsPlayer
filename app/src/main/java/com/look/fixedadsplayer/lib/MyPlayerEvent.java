package com.look.fixedadsplayer.lib;


import com.google.ads.interactivemedia.v3.api.AdEvent;

public interface MyPlayerEvent extends AdEvent.AdEventListener {
    /**
     * Player started buffering data
     */
    void onBuffering();

    /**
     * Player ready to play
     */
    void onReady(boolean isPlaying, long playerTotalDurationInSecond);

    /**
     * Player Completed content
     */
    void onCompleted();

    /**
     * Player Error Occur
     */
    void onError();

    /**
     * Display Next Video  Title
     */
    default void onShowEndSkipButton() {

    }
    /**
     * Display Next Video  Title
     */
    default void onHideEndSkipButton() {

    }

    /**
     * Display Next Video  Title
     */
    default void onShowNextVideoOverlay() {

    }

    /**
     * Hide Next Video  Title
     */
    default void onHideNextVideoOverlay() {

    }

    /**
     * Call on Every Second
     */
    default void playerEverySecondVOD(int playerDurationInSecond) {

    }

    /**
     * Call on Every Second
     */
    default void playerEverySecondLive(int playerDurationInSecond) {

    }
    /**
     * Call on Ad Complete
     */
    default void onVideoPlay() {

    }

    default void onVideoPause() {

    }

    /**
     * Call on Ad Complete
     */
    default void adCompleted() {

    }

    /**
     * Call on Ad Complete
     */
    default void adError() {

    }

    /**
     * Call on Ad Complete
     */
    default void adStart(long l) {

    }

    void onEverySecond(boolean b);
}
