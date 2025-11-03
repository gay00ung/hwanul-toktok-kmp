package net.ifmain.hwanultoktok.kmp.presentation.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import net.ifmain.hwanultoktok.kmp.BuildConfig

private const val TAG = "InterstitialAds"

private class AndroidInterstitialAdController(
    private val context: Context,
    private val adUnitId: String
) : InterstitialAdController {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false

    override fun preload() {
        if (interstitialAd != null || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    Log.w(TAG, "Failed to load interstitial: ${error.code} ${error.message}")
                }
            }
        )
    }

    override fun show(onFinished: () -> Unit) {
        val activity = context.findActivity()
        val ad = interstitialAd

        if (activity == null || ad == null) {
            preload()
            onFinished()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed")
                interstitialAd = null
                preload()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Failed to show interstitial: ${adError.code} ${adError.message}")
                interstitialAd = null
                preload()
                onFinished()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial shown")
                interstitialAd = null
            }
        }

        interstitialAd = null
        ad.show(activity)
    }

    private tailrec fun Context.findActivity(): Activity? =
        when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> null
        }
}

@Composable
actual fun rememberInterstitialAdController(): InterstitialAdController? {
    val context = LocalContext.current
    val controller = remember {
        AndroidInterstitialAdController(
            context = context,
            adUnitId = BuildConfig.ADMOB_INTERSTITIAL_ID
        )
    }

    LaunchedEffect(Unit) {
        controller.preload()
    }

    return controller
}
