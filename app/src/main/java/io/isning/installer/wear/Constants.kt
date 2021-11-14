package io.isning.installer.wear

import android.graphics.Bitmap

object Constants {

    private const val PKG_NAME = "io.isning.installer.installer"
    const val SP_FILE_NAME = "config"
    const val APK_SOURCE = "apkSource"
    const val APK_INFO = "apkInfo"
    const val IS_FORM_INSTALL_ACT = "isFormInstallAct"
    const val URI_DATA_TYPE = "application/vnd.android.package-archive"
    const val DEFAULT_SYS_PKG_NAME = "com.android.packageinstaller"
    const val PROVIDER_STR = "$PKG_NAME.FileProvider"
    const val SE_LINUX_COMMAND = "setenforce permissive"
    const val INSTALL_COMMAND = "pm install -r -d --user 0 -i $PKG_NAME %s"
    const val COPY_COMMAND = "cp %s %s"
    const val UNINSTALL_COMMAND = "pm uninstall "
    const val ANDROID_DATA_STR = "Android/data"

    val TRANS_FILE_PATH = App.context.getExternalFilesDir(null)?.absolutePath!! + "/temp.apk"
    const val DATA_LOCAL_TMP_STR = "/data/local/tmp"
    const val TMP_PACKAGE_FILE_NAME = "temp.apk"

    const val KEY_ADB_KEYPAIR_PUB = "adb_keypair_pub"
    const val KEY_ADB_KEYPAIR_PRI = "adb_keypair_pri"
    const val KEY_ALGORITHM = "RSA"

    const val PERM_CODE = 955
    const val API_R_CODE = 11

    var APP_ICON_BITMAP: Bitmap? = null

}
