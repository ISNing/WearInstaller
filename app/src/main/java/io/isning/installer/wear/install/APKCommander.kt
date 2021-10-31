package io.isning.installer.wear.install

import android.net.Uri
import android.util.Log

import io.isning.installer.wear.data.ApkInfoEntity
import io.isning.installer.wear.utils.doAsync
import io.isning.installer.wear.utils.onUI

class APKCommander {

    private var installCallback: InstallCallback
    private lateinit var apkInfoEntity: ApkInfoEntity

    private var uri: Uri? = null
    private var referrer: String? = null

    constructor(apkInfoEntity: ApkInfoEntity, installCallback: InstallCallback) {
        this.apkInfoEntity = apkInfoEntity
        this.installCallback = installCallback
    }

    constructor(uri: Uri, referrer: String, installCallback: InstallCallback) {
        this.uri = uri
        this.referrer = referrer
        this.installCallback = installCallback
    }

    fun start() {
        if (this::apkInfoEntity.isInitialized) {
            installCallback.onApkParsed(apkInfoEntity)
            return
        }
        ParseApkTask(uri!!, referrer!!).let {
            doAsync({
                Log.e("ParseApkError->", it.message!!)
            }, {
                apkInfoEntity = it.startParseApkTask()
                onUI {
                    installCallback.onApkParsed(apkInfoEntity)
                }
            })
        }
    }

    fun startInstall() {
            InstallApkAdbTask(apkInfoEntity, installCallback).start()
    }

}
