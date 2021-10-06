# FixedAdsPlayer
FixedAdsPlayer is an application level media player for Android by using Exoplayer library. 
It provides an
alternative to Android’s IMA SDK for playing video.
You can play videos as Ads like IMA SDK.

<ul>
<li>Feature:</li>
<li>Play video url as Ads</li>
<li>You can play Ads on start ,mid and end position</li>
<li>Skip button is working when video length greater then 30 second</li>
<li>Enjoy it.</li>

<li> this.fixedAdsLoader = new FixedAdsLoader(activity);</li>
</ul>
        
        ```gradle
        player = new SimpleExoPlayer.Builder(activity.getApplication()).setMediaSourceFactory(mediaSourceFactory).build();
        Uri contentUri = Uri.parse(contentUrl);
        Uri adTagUri = Uri.parse(FIXED_ADS_TAG);
        fixedAdsLoader.startAdsUrl(start);
        fixedAdsLoader.midAdsUrl(midUrl, 0);
        fixedAdsLoader.endAdsUrl(endUrl);
        MediaItem mediaItem = new MediaItem.Builder().setUri(contentUri).setAdTagUri(adTagUri).build();
        fixedAdsLoader.setPlayer(player);
        playerView.setPlayer(player);
        ```


        


Result

![track_location_device_devdeeds](https://raw.githubusercontent.com/gautamlook/LocationTracker/main/device-2021-09-19-121719.png)

![track_location_device_devdeeds](https://raw.githubusercontent.com/gautamlook/LocationTracker/main/device-2021-09-19-121742.png)

![track_location_device_devdeeds](https://raw.githubusercontent.com/gautamlook/LocationTracker/main/device-2021-09-19-124427.png)
