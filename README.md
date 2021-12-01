<h1>AmazicAdsLibraty</h1>
<h3><li>Adding the library to your project: Add the following in your root build.gradle at the end of repositories:</br></h3>

maven { url 'https://jitpack.io' } </br>
implementation 'com.github.quangchienictu:AmazicAdsLibrary:Tag'


<h2>- BannerAds</h2>
<div class="content">
  <h4>View xml</h4>
<pre><code>< include
        android:id="@+id/include"
        layout="@layout/layout_banner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        app:layout_constraintBottom_toBottomOf="parent" /> 
   
 </code></pre>
<h4>Load in ativity</h4>
<pre>
  <code>
    Admod.getInstance().loadBanner(this,"bannerID");
  </code>
</pre>
<h4>Load in fragment</h4>
<pre>
  <code>
   Admod.getInstance().loadBannerFragment( mActivity, "bannerID",  rootView)
  </code>
</pre>
</div>
<h2>IntertitialAds</h2>
<div class="content">
  <h3>- Inter Splash</h3>
  <pre>
    <code>
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
    </code>
  </pre>
<h3>- InterstitialAds</h3>
  <h4>Create and load interstitialAds</h4>
<pre><code>
  private InterstitialAd mInterstitialAd;

   Admod.getInstance().loadInterAds(this, "interstitial_id" new InterCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });
</code></pre>
<h4>Show interstitialAds</h4>
<pre><code>
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
</code></pre>
</div>

<h2>- RewardAds</h2>
<div class="content">
  <h4>Init RewardAds</h4>
<pre><code>  Admod.getInstance().initRewardAds(this,reward_id);</code></pre>
<h4>Show RewardAds</h4>
<pre><code>
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
</code></pre>
</div>

<h2>- NativeAds</h2>
<div class="content">
  <h4>View xml</h4>
<pre>
  <code>
    < FrameLayout
        android:id="@+id/native_ads"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
  </code>
</pre>
<h4>Create and show nativeAds</h4>
<pre>
  <code>
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
  </code>
</pre>

</div>
