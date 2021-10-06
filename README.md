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
       </ul> 

To get a Git project into your build:
Add it in your root build.gradle at the end of repositories:
```
Step 1. Add the JitPack repository to your build file 

  allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
 ```
 ```
Step 2. Add the dependency   
    
    dependencies {
	        implementation 'com.github.gautamlook:FixedAdsPlayer:v1.1'
	}
 
 ``` 
 
     ```
        this.fixedAdsLoader = new FixedAdsLoader(activity);
        player = new SimpleExoPlayer.Builder(activity.getApplication()).setMediaSourceFactory(mediaSourceFactory).build();
        // Media url
        Uri contentUri = Uri.parse(contentUrl);
        Uri adTagUri = Uri.parse(FIXED_ADS_TAG);
        // First video url for ad
        fixedAdsLoader.startAdsUrl(start);
         // second video url for ad
        fixedAdsLoader.midAdsUrl(midUrl, 0);
         // third video url for ad
        fixedAdsLoader.endAdsUrl(endUrl);
        MediaItem mediaItem = new MediaItem.Builder().setUri(contentUri).setAdTagUri(adTagUri).build();
        fixedAdsLoader.setPlayer(player);
        playerView.setPlayer(player);
        ```
 
Result
Ad with time elapse
![track_location_device_devdeeds](https://raw.githubusercontent.com/gautamlook/FixedAdsPlayer/main/ad2.png)
Ad with skip button
![track_location_device_devdeeds](https://raw.githubusercontent.com/gautamlook/FixedAdsPlayer/main/ad3.png)

