package com.amazic.ads.util

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import com.amazic.ads.callback.AdCallback
import com.amazic.ads.callback.ApiCallBack
import com.amazic.ads.callback.BannerCallBack
import com.amazic.ads.callback.InterCallback
import com.amazic.ads.iap.BillingCallback
import com.amazic.ads.iap.IAPManager
import com.amazic.ads.iap.ProductDetailCustom
import com.amazic.ads.organic.TechManager
import com.amazic.ads.service.AdmobApi
import com.amazic.ads.util.manager.banner.BannerBuilder
import com.amazic.ads.util.manager.banner.BannerManager
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AsyncSplash {
    private val TAG = "AsyncSplash"
    private var isTech = false
    private var adsSplash: AdsSplash? = null
    private var jsonIdAdsDefault = ""
    private var adjustKey = ""
    private var linkServer = ""
    private var appId = ""
    private var isShowAdsSplashOrNextAct = false
    private var initWelcomeBack = "Normal"
    private var welcomeBackClass: Class<*>? = null
    private var isShowBannerSplash = true
    private var frAdsBannerSplash: FrameLayout? = null
    private var listIdBannerSplash: MutableList<String> = arrayListOf("ca-app-pub-3940256099942544/6300978111")
    private var adsKey: String = ""
    private var listTurnOffRemoteKeys: MutableList<String> = mutableListOf()
    private var activity: AppCompatActivity? = null
    private var interCallback: InterCallback? = null
    private var appOpenCallback: AdCallback? = null
    private var isDebug = false
    private var isUseBilling = false
    private var listProductDetailCustoms: ArrayList<ProductDetailCustom> = arrayListOf()
    private var timeOutSplash = 12000L

    //use for log event
    private var timeStartSplash = System.currentTimeMillis()

    companion object {
        private var INSTANCE: AsyncSplash? = null
        fun getInstance(): AsyncSplash {
            if (INSTANCE == null) {
                INSTANCE = AsyncSplash()
            }
            return INSTANCE as AsyncSplash
        }
    }

    fun init(activity: AppCompatActivity, appOpenCallback: AdCallback, interCallback: InterCallback, adjustKey: String, linkServer: String, appId: String, jsonIdAdsDefault: String) {
        resetVarToDefault()
        this.activity = activity
        this.adjustKey = adjustKey
        this.jsonIdAdsDefault = jsonIdAdsDefault
        this.linkServer = linkServer
        this.appId = appId
        this.appOpenCallback = appOpenCallback
        this.interCallback = interCallback
    }

    private fun resetVarToDefault() {
        this.isTech = false
        this.jsonIdAdsDefault = ""
        this.adjustKey = ""
        this.linkServer = ""
        this.appId = ""
        this.isShowAdsSplashOrNextAct = false
        this.initWelcomeBack = "Normal"
        this.welcomeBackClass = null
        this.isShowBannerSplash = true
        this.listIdBannerSplash = arrayListOf("ca-app-pub-3940256099942544/6300978111")
        this.listTurnOffRemoteKeys = mutableListOf()
        this.isDebug = false
        this.isUseBilling = false
        this.listProductDetailCustoms = arrayListOf()
        this.timeOutSplash = 12000L
    }

    fun getTimeStartSplash(): Long {
        return this.timeStartSplash
    }

    fun setTimeOutSplash(timeOutSplash: Long) {
        this.timeOutSplash = timeOutSplash
    }

    fun setUseBilling(listProductDetailCustoms: ArrayList<ProductDetailCustom>) {
        this.isUseBilling = true
        this.listProductDetailCustoms.clear()
        this.listProductDetailCustoms.addAll(listProductDetailCustoms)
    }

    fun setDebug(isDebug: Boolean) {
        this.isDebug = isDebug
    }

    fun checkShowSplashWhenFail() {
        if (adsSplash != null) {
            adsSplash?.onCheckShowSplashWhenFail(appOpenCallback, interCallback)
        }
    }

    fun setInitResumeAdsNormal() {
        this.initWelcomeBack = "Normal"
    }

    fun setShowBannerSplash(isShowBannerSplash: Boolean, frAdsBannerSplash: FrameLayout, listIdBannerSplash: MutableList<String>, adsKey: String) {
        this.isShowBannerSplash = isShowBannerSplash
        this.frAdsBannerSplash = frAdsBannerSplash
        this.listIdBannerSplash.clear()
        this.listIdBannerSplash.addAll(listIdBannerSplash)
        this.adsKey = adsKey
    }

    fun setListTurnOffRemoteKeys(listTurnOffRemoteKeys: MutableList<String>) {
        this.listTurnOffRemoteKeys.clear()
        this.listTurnOffRemoteKeys.addAll(listTurnOffRemoteKeys)
    }

    fun handleAsync(lifecycleOwner: LifecycleOwner, lifecycleCoroutineScope: LifecycleCoroutineScope, onNoInternetAction: () -> Unit) {
        timeStartSplash = System.currentTimeMillis()
        lifecycleCoroutineScope.launch {
            delay(timeOutSplash)
            if (!isShowAdsSplashOrNextAct) {
                //increase splash open
                SharePreferenceHelper.setInt(activity, EventTrackingHelper.splash_open, SharePreferenceHelper.getInt(activity, EventTrackingHelper.splash_open, 1) + 1)
                //end increase splash open
                if (isTech) {
                    turnOffSomeRemoteKeys(activity)
                }
                interCallback?.onNextAction()
                Log.d(TAG, "Timeout Splash.")
                isShowAdsSplashOrNextAct = true
            }
            return@launch
        }
        if (NetworkUtil.isNetworkActive(activity)) {
            lifecycleCoroutineScope.launch {
                val asyncAdmobApi = async { initAdmobApi(activity) }
                val asyncRemoteConfig = async { initRemoteConfig(activity) }
                val asyncUMP = async { initAdsConsentManager(activity) }
                val asyncBilling = async { initBilling() }
                val asyncTechManager = async { initTechManager(activity) }
                try {
                    //wait to load banner splash (banner splash fix id, don't use api to reduce time load splash)
                    awaitAll(asyncRemoteConfig, asyncUMP, asyncBilling, asyncTechManager)
                    if (isTech) {
                        turnOffSomeRemoteKeys(activity)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    loadBannerSplash(activity, lifecycleOwner, frAdsBannerSplash, listIdBannerSplash, adsKey)
                }
                try {
                    //wait to load inter or open splash
                    asyncAdmobApi.await()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    showAdsSplash(activity, appOpenCallback, interCallback)
                }
            }
        } else {
            if (!isShowAdsSplashOrNextAct) {
                onNoInternetAction.invoke()
                isShowAdsSplashOrNextAct = true
            }
        }
    }

    private fun turnOffSomeRemoteKeys(activity: AppCompatActivity?) {
        listTurnOffRemoteKeys.forEach {
            Log.d(TAG, "turnOffSomeRemoteKeys: $it")
            RemoteConfigHelper.getInstance().set_config(activity, it, false)
        }
    }


    private suspend fun initRemoteConfig(activity: AppCompatActivity?) = suspendCoroutine<Unit> { continuation ->
        RemoteConfigHelper.getInstance().fetchAllKeysAndTypes(activity) {
            Admob.getInstance().setOpenShowAllAds(RemoteConfigHelper.getInstance().get_config(activity, RemoteConfigHelper.show_all_ads))
            Admob.getInstance().setTimeInterval(
                RemoteConfigHelper.getInstance().get_config_long(activity, RemoteConfigHelper.interval_between_interstitial) * 1000
            )
            continuation.resume(Unit)
            Log.d(TAG, "initRemoteConfig.")
        }
    }

    private suspend fun initAdsConsentManager(activity: AppCompatActivity?) = suspendCoroutine<Unit> { continuation ->
        val adsConsentManager = AdsConsentManager(activity)
        adsConsentManager.requestUMP {
            if (it) {
                Admob.getInstance().initAdmod(activity) {}
                activity?.let { it1 -> AppOpenManager.getInstance().disableAppResumeWithActivity(it1.javaClass) }
            }
            continuation.resume(Unit)
            Log.d(TAG, "initAdsConsentManager.")
        }
    }

    private suspend fun initTechManager(activity: AppCompatActivity?) = suspendCoroutine<Unit> { continuation ->
        TechManager.getInstance().getResult(isDebug, activity, adjustKey) {
            if (it) {
                isTech = true
                AppOpenManager.getInstance().disableAppResume()
            }
            continuation.resume(Unit)
            Log.d(TAG, "initTechManager.")
        }
    }

    private suspend fun initAdmobApi(activity: AppCompatActivity?) = suspendCoroutine<Unit> { continuation ->
        AdmobApi.getInstance().jsonIdAdsDefault = jsonIdAdsDefault
        AdmobApi.getInstance().timeOutCallApi = 4000
        AdmobApi.getInstance().init(activity, linkServer, appId, object : ApiCallBack() {
            override fun onReady() {
                super.onReady()
                when (initWelcomeBack) {
                    "Normal" -> {
                        if (AdmobApi.getInstance().listIDAppOpenResume.isNotEmpty()) {
                            AppOpenManager.getInstance().initApi(activity?.application)
                            activity?.let { AppOpenManager.getInstance().disableAppResumeWithActivity(it.javaClass) } //disable resume splash
                        }
                    }

                    else -> {
                        if (AdmobApi.getInstance().listIDAppOpenResume.isNotEmpty()) {
                            AppOpenManager.getInstance().initApi(activity?.application)
                            activity?.let { AppOpenManager.getInstance().disableAppResumeWithActivity(it.javaClass) } //disable resume splash
                        }
                    }
                }
                continuation.resume(Unit)
                Log.d(TAG, "initAdmobApi.")
            }
        })
    }

    private suspend fun initBilling() = suspendCoroutine<Unit> { continuation ->
        if (isUseBilling) {
            //check if app use billing -> initBilling
            IAPManager.getInstance().initBilling(activity, listProductDetailCustoms, object : BillingCallback() {
                private var isResumed = false
                override fun onBillingSetupFinished(resultCode: Int) {
                    super.onBillingSetupFinished(resultCode)
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(Unit)
                        Log.d(TAG, "initBilling.")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    super.onBillingServiceDisconnected()
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(Unit)
                        Log.d(TAG, "initBilling.")
                    }
                }
            })
        } else {
            continuation.resume(Unit)
            Log.d(TAG, "Not use billing.")
        }
    }

    private fun loadBannerSplash(activity: AppCompatActivity?, lifecycleOwner: LifecycleOwner, frAdsBanner: FrameLayout?, listIdBannerSplash: MutableList<String>, adsKey: String) {
        if (isShowBannerSplash) {
            frAdsBanner?.visibility = View.VISIBLE
            val bannerBuilder = BannerBuilder()
            bannerBuilder.setListId(listIdBannerSplash)
            bannerBuilder.callBack = object : BannerCallBack() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError?) {
                    super.onAdFailedToLoad(loadAdError)
                    frAdsBanner?.visibility = View.GONE
                }
            }
            activity?.let { BannerManager(it, lifecycleOwner, bannerBuilder, adsKey) }
        } else {
            frAdsBanner?.visibility = View.GONE
        }
    }

    private fun showAdsSplash(activity: AppCompatActivity?, appOpenCallback: AdCallback?, interCallback: InterCallback?) {
        if (!isShowAdsSplashOrNextAct) {
            var rateAoaInterSplash: String = RemoteConfigHelper.getInstance().get_config_string(activity, RemoteConfigHelper.rate_aoa_inter_splash)
            if (rateAoaInterSplash.isEmpty()) {
                rateAoaInterSplash = "0_100"
            }
            val isShowOpenSplash: Boolean = RemoteConfigHelper.getInstance().get_config(activity, RemoteConfigHelper.open_splash)
            val isShowInterSplash: Boolean = RemoteConfigHelper.getInstance().get_config(activity, RemoteConfigHelper.inter_splash)
            adsSplash = AdsSplash.init(activity, isShowOpenSplash, isShowInterSplash, rateAoaInterSplash)
            adsSplash?.showAdsSplashApi(appOpenCallback, interCallback)
            Log.d(TAG, "showAdsSplash.")
            isShowAdsSplashOrNextAct = true
        }
    }
}