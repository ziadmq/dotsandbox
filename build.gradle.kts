buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
    dependencies {
        // أضف هذا السطر ليعرف بلجن هواوي إصدار الأندرويد (تأكد من مطابقة الإصدار 8.10.1)
        classpath("com.android.tools.build:gradle:8.10.1")
        // بلجن هواوي
        classpath("com.huawei.agconnect:agcp:1.9.1.301")
    }
}// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}