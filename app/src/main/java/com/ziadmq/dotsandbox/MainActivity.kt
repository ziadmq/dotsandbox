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
import com.huawei.hms.ads.*
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.ads.interstitial.InterstitialAd
import androidx.compose.ui.viewinterop.AndroidView
import com.ziadmq.dotsandbox.view.MainApp

class MainActivity : ComponentActivity() {

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HwAds.init(this)
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
        mInterstitialAd = InterstitialAd(this)
        // ملاحظة: ضع الـ Unit ID الخاص بالإعلان البيني هنا (Interstitial)
        mInterstitialAd?.adId = "YOUR_INTERSTITIAL_ID"
        val adParam = AdParam.Builder().build()
        mInterstitialAd?.loadAd(adParam)
    }

    private fun showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd!!.isLoaded) {
            mInterstitialAd?.show(this)
            loadInterstitial()
        } else {
            loadInterstitial()
        }
    }
}

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            BannerView(context).apply {
                // معرف البنر من الصورة التي أرسلتها
                adId = "s3kqgbuk9q"
                bannerAdSize = BannerAdSize.BANNER_SIZE_320_50
                loadAd(AdParam.Builder().build())
            }
        }
    )
}


