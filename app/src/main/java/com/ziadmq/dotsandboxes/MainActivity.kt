package com.ziadmq.dotsandboxes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ziadmq.dotsandboxes.ui.theme.*
import com.huawei.hms.ads.*
import com.ziadmq.dotsandboxes.view.MainApp

class MainActivity : ComponentActivity() {

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. تهيئة إعلانات هواوي
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
        // 2. وضع معرف الإعلان الصحيح الذي استخرجته
        mInterstitialAd?.adId = "s3kqgbuk9q"
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

// ملاحظة: قمنا بحذف AdBanner لأن المعرف s3kqgbuk9q هو لإعلان ملء شاشة وليس بنر