package io.isning.installer.wear.install

import io.isning.installer.wear.data.ApkInfoEntity

interface InstallCallback {

    fun onApkParsed(apkInfo: ApkInfoEntity)

    fun onApkPreInstall()

    fun onApkInstalled(installStatus: InstallStatus)

    fun onInstallLog(installLog: String)

}
