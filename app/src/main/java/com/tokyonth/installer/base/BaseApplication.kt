package com.tokyonth.installer.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

import androidx.appcompat.app.AppCompatDelegate

import com.tokyonth.installer.Constants
import com.tokyonth.installer.utils.SPUtils

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        SPUtils.getInstance(this, Constants.SP_FILE_NAME)

        val nightMode = if (SPUtils.getData(Constants.SP_NIGHT_MODE, false) as Boolean)
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set

    }

}
