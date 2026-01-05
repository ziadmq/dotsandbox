package com.ziadmq.dotsandbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ziadmq.dotsandbox.ui.theme.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import androidx.compose.ui.viewinterop.AndroidView
import com.ziadmq.dotsandbox.view.MainApp

class MainActivity : ComponentActivity() {

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        loadInterstitial()

        setContent {
            DotsAndBoxTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF141E30), Color(0xFF243B55))
                            )
                        )
                ) {
                    MainApp(onShowInterstitial = { showInterstitial() })
                }
            }
        }
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this, "ca-app-pub-2172903105244124/6242743555", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }

    private fun showInterstitial() {
        mInterstitialAd?.let {
            it.show(this)
            loadInterstitial() // Reload for next time
        } ?: run {
            loadInterstitial()
        }
    }
}

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-2172903105244124/3371848266"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}


