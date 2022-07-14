<h1>AmazicAdsLibraty</h1>
<h3><li>Adding the library to your project: Add the following in your root build.gradle at the end of repositories:</br></h3>

<pre>
  allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }	    
    }
}
</pre>
<h5>Implement library in your app level build.gradle:</h5>
<pre>
 dependencies {
    implementation 'com.github.quangchienictu:AmazicAdsLibrary:{version}'
    implementation 'com.google.android.gms:play-services-ads:20.5.0'
    //multidex
    implementation "androidx.multidex:multidex:2.0.1"
  }

  defaultConfig {
    multiDexEnabled true
  }
</pre>
<h3><li>Add app id in Manifest:</br></h3>
<pre>
     < meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-4973559944609228~2346710863" />
</pre>
<h3><li>Init aplication</br></h3>
<pre> < application
   android:name=".MyApplication"
   .
   .
   .../></pre>
<pre>
    public class MyApplication extends AsdApplication {
    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return "resume_id";
    }
}

</pre>
<h2>- BannerAds</h2>
<div class="content">
  <h4>View xml</h4>
<pre>< include
        android:id="@+id/include"
        layout="@layout/layout_banner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        app:layout_constraintBottom_toBottomOf="parent" /> 
   
 </pre>
<h4>Load in ativity</h4>
<pre>
  
    Admod.getInstance().loadBanner(this,"bannerID");
  
</pre>
<h4>Load in fragment</h4>
<pre>
  
   Admod.getInstance().loadBannerFragment( mActivity, "bannerID",  rootView)
  
</pre>
</div>
<h2>IntertitialAds</h2>
<div class="content">
  <h3>- Inter Splash</h3>
  <pre>
    
      Admod.getInstance().loadSplashInterAds(this,"interstitial_id",25000,5000, new InterCallback(){
            @Override
            public void onAdClosed() {
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }
        });
    
  </pre>
<h3>- InterstitialAds</h3>
  <h4>Create and load interstitialAds</h4>
<pre>
  private InterstitialAd mInterstitialAd;

   Admod.getInstance().loadInterAds(this, "interstitial_id" new InterCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });
</pre>
<h4>Show interstitialAds</h4>
<pre>
   Admod.getInstance().showInterAds(MainActivity.this, mInterstitialAd, new InterCallback() {
                    @Override
                    public void onAdClosed() {
                        startActivity(new Intent(MainActivity.this,MainActivity3.class));
                        // Create and load interstitialAds (when not finish activity ) 
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        startActivity(new Intent(MainActivity.this,MainActivity3.class));
                         // Create and load interstitialAds (when not finish activity) 
                    }

                });
</pre>
</div>

<h2>- RewardAds</h2>
<div class="content">
  <h4>Init RewardAds</h4>
<pre>  Admod.getInstance().initRewardAds(this,reward_id);</pre>
<h4>Show RewardAds</h4>
<pre>
  Admod.getInstance().showRewardAds(MainActivity.this,new RewardCallback(){
                    @Override
                    public void onEarnedReward(RewardItem rewardItem) {
                        // code here
                    }

                    @Override
                    public void onAdClosed() {
                        // code here
                    }

                    @Override
                    public void onAdFailedToShow(int codeError) {
                       // code here
                    }
                });
</pre>
</div>

<h2>- NativeAds</h2>
<div class="content">
  <h4>View xml</h4>
<pre>
  
    < FrameLayout
        android:id="@+id/native_ads"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
  
</pre>
<h4>Create and show nativeAds</h4>
<pre>
  
     private FrameLayout native_ads;
     
     native_ads = findViewById(R.id.native_ads);
     
      Admod.getInstance().loadNativeAd(this, "native_id", new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = ( NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_native, null);
                native_ads.addView(adView);
                Admod.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
  
</pre>

</div>

<h3><li>Set limit time when click ads</br></h3>
<pre>
 AppIronSource.getInstance().setTimeLimit(60000);
 Admod.getInstance().setTimeLimit(60000);
</pe>
