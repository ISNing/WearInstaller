package io.isning.installer.wear.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import io.isning.installer.wear.App

import io.isning.installer.wear.Constants
import io.isning.installer.wear.R
import io.isning.installer.wear.install.APKCommander
import io.isning.installer.wear.data.ApkInfoEntity
import io.isning.installer.wear.install.InstallCallback
import io.isning.installer.wear.install.InstallStatus
import io.isning.installer.wear.utils.CommonUtils
import io.isning.installer.wear.utils.NotificationUtil
import java.io.File

class SilentlyInstallActivity : BaseActivity(), InstallCallback {

    private lateinit var apkCommander: APKCommander
    private lateinit var apkInfoEntity: ApkInfoEntity

    private var installLog = ""

    override fun initView(): ViewBinding? {
        if (App.localData.isFirstBoot()) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
        return null
    }

    override fun initData() {
        val apkSource = CommonUtils.reflectGetReferrer(this)!!
        if (intent.getBooleanExtra(Constants.IS_FORM_INSTALL_ACT, false)) {
            apkInfoEntity = intent.getParcelableExtra(Constants.APK_INFO)!!
            apkCommander = APKCommander(apkInfoEntity, this)
            apkCommander.start()
            return
        }
        intent.data.let { uri ->
            if (uri == null) {
                showToast(getString(R.string.unable_install_apk))
                finish()
            } else {
                if (App.localData.isDefaultSilent()) {
                    apkCommander = APKCommander(uri, apkSource, this)
                    apkCommander.start()
                    return
                }
                Intent(this, InstallerActivity::class.java).let {
                    it.data = uri
                    it.putExtra(Constants.APK_SOURCE, apkSource)
                    startActivity(it)
                    finish()
                }
            }
        }
    }

    override fun onApkParsed(apkInfo: ApkInfoEntity) {
        if (!apkInfo.packageName.isNullOrEmpty()) {
            apkInfoEntity = apkInfo
            apkCommander.startInstall()
        } else {
            showToast(getString(R.string.unable_install_apk))
            finish()
        }
    }

    override fun onApkPreInstall() {
        showToast(getString(R.string.start_install, apkInfoEntity.appName))
    }

    override fun onApkInstalled(installStatus: InstallStatus) {
        var notificationSub = apkInfoEntity.appName!!
        val status = when (installStatus) {
            InstallStatus.SUCCESS -> {
                if (App.localData.isAutoDel()) {
                    File(apkInfoEntity.filePath!!).delete()
                    notificationSub = (getString(R.string.auto_del_notification, notificationSub))
                }
                getString(R.string.install_successful)
            }
            InstallStatus.FAILURE -> {
                notificationSub += " ($installLog)"
                getString(R.string.install_failed_msg)
            }
        }
        NotificationUtil().sendNotification(this, status, notificationSub, apkInfoEntity.getIcon()!!)
        finish()
    }

    override fun onInstallLog(installLog: String) {
        this.installLog = installLog
        Log.d("SilentlyInstallActivity", installLog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val attr = window.attributes.apply {
            height = 0
            width = 0
        }
        window.apply {
            attributes = attr
            setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

}
