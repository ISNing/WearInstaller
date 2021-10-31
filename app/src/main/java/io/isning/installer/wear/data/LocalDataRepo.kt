package io.isning.installer.wear.data

import android.content.Context
import io.isning.installer.wear.BuildConfig
import io.isning.installer.wear.utils.SPUtils.get
import io.isning.installer.wear.utils.SPUtils.set

class LocalDataRepo(val context: Context) {

    fun setDefaultSilent(boolean: Boolean) {
        context["defaultSilent"] = boolean
    }

    fun isDefaultSilent(): Boolean {
        return context["defaultSilent", false]
    }

    fun setAutoDel(boolean: Boolean) {
        context["autoDelete"] = boolean
    }

    fun isAutoDel(): Boolean {
        return context["autoDelete", false]
    }

    fun setShowPermission(boolean: Boolean) {
        context["showPermission"] = boolean
    }

    fun isShowPermission(): Boolean {
        return context["showPermission", true]
    }

    fun setShowActivity(boolean: Boolean) {
        context["showActivity"] = boolean
    }

    fun isShowActivity(): Boolean {
        return context["showActivity", true]
    }

    fun setNightMode(boolean: Boolean) {
        context["nightMode"] = boolean
    }

    fun isNightMode(): Boolean {
        return context["nightMode", false]
    }

    fun setFollowSystem(boolean: Boolean) {
        context["nightFollowSystem"] = boolean
    }

    fun isFollowSystem(): Boolean {
        return context["nightFollowSystem", false]
    }

    fun setNeverShowTip() {
        context["neverVersionTips"] = true
    }

    fun isNeverShowTip(): Boolean {
        return context["neverVersionTips", false]
    }

    fun setNotFirstBoot() {
        context["isFirstBootTAG"] = BuildConfig.VERSION_NAME
    }

    fun isFirstBoot(): Boolean {
        if (BuildConfig.needRemindUser) {
            return context["isFirstBootTAG", ""] != BuildConfig.VERSION_NAME
        }
        return false
    }
}
