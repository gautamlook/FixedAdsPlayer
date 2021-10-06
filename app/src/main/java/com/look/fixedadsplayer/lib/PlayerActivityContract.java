package com.look.fixedadsplayer.lib;

import android.app.Dialog;
import android.os.Bundle;

import com.google.ads.interactivemedia.v3.api.AdEvent;



public class PlayerActivityContract {


    interface PlayerActivityPresenter{
        void setDurationMarker(String assetsID, long videoCurrentPosition, String userId,String type,String tvI);

    }

    public interface PlayerActivityView extends MyPlayerEvent
            {


        @Override
        default void onBuffering() {

        }

        @Override
        default void onReady(boolean isPlaying, long playerTotalDurationInSecond) {

        }

        @Override
        default void onCompleted() {

        }

        @Override
        default void onError() {

        }

        @Override
        default void onShowNextVideoOverlay() {

        }

        @Override
        default void onHideNextVideoOverlay() {

        }

        @Override
        default void playerEverySecondVOD(int playerDurationInSecond) {

        }

        @Override
        default void playerEverySecondLive(int playerDurationInSecond) {

        }

        void onCreate(Bundle savedInstanceState);

        @Override
        default void adCompleted() {

        }

        @Override
        default void adError() {

        }

        @Override
        default void adStart(long l) {

        }

        @Override
        default void onAdEvent(AdEvent adEvent) {

        }
    }
}
