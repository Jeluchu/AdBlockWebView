package com.jeluchu.adblocker

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import org.adblockplus.libadblockplus.android.AdblockEngine
import org.adblockplus.libadblockplus.android.AndroidHttpClientResourceWrapper
import org.adblockplus.libadblockplus.android.settings.AdblockHelper
import java.util.*

class AdBlockerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (!AdblockHelper.get().isInit) {
            val basePath = getDir(AdblockEngine.BASE_PATH_DIRECTORY, Context.MODE_PRIVATE).absolutePath
            val map: MutableMap<String, Int> = HashMap()
            map[AndroidHttpClientResourceWrapper.EASYLIST] = R.raw.easylist
            map[AndroidHttpClientResourceWrapper.ACCEPTABLE_ADS] = R.raw.exceptionrules
            AdblockHelper
                    .get()
                    .init(this, basePath, false, AdblockHelper.PREFERENCE_NAME)
                    .preloadSubscriptions(AdblockHelper.PRELOAD_PREFERENCE_NAME, map)
        }
    }

}