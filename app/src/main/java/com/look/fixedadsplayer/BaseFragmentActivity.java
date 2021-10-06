package com.look.fixedadsplayer;

import android.content.Context;
import android.media.AudioManager;

import androidx.fragment.app.FragmentActivity;


/*
 * Created by developer on 6/6/2019.
 */
public abstract class BaseFragmentActivity extends FragmentActivity implements AudioManager.OnAudioFocusChangeListener {

    @Override
    public void onAudioFocusChange(int focusChange) {
//        LogUtil.getInstance().i("onAudioFocusChange Page", "=====" + focusChange);
    }

    protected void onResume() {
        super.onResume();
        try {

            AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null && AudioManager.AUDIOFOCUS_REQUEST_GRANTED != audioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
