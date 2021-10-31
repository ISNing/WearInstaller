package io.isning.installer.wear.install

import android.util.Base64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import com.tananaev.adblib.AdbStream
import io.isning.installer.wear.App
import io.isning.installer.wear.Constants
import io.isning.installer.wear.data.ApkInfoEntity
import io.isning.installer.wear.utils.CommonUtils
import io.isning.installer.wear.utils.FileIOUtils
import io.isning.installer.wear.utils.doAsync
import io.isning.installer.wear.utils.onUI
import io.isning.installer.wear.utils.path.ParsingContentUtil
import java.io.File
import java.io.IOException
import java.net.Socket

class InstallApkAdbTask constructor(private val apkInfoEntity: ApkInfoEntity,
                                    private val installCallback: InstallCallback) {

    fun start() {
        installCallback.onApkPreInstall()
        doAsync {
            try {
                val curFilePath = apkInfoEntity.filePath!!
                val transFilePath = Constants.TRANS_FILE_PATH
                val tgtFilePath = Constants.DATA_LOCAL_TMP_STR + "/" + Constants.TMP_PACKAGE_FILE_NAME
                val fromFile = ParsingContentUtil(apkInfoEntity.uriReferrer).getFile(App.context, apkInfoEntity.fileUri)
                val transFile = File(transFilePath)

                onUI {
                    installCallback.onInstallLog("Copying File $curFilePath to $transFilePath for transferring\n")
                }
                try {
                    FileIOUtils.copyFile(fromFile, transFile, true)
                }catch (ex: Exception) {
                    onUI {
                        installCallback.onInstallLog("File copy failed\n\n")
                        throw ex
                    }
                }
                onUI {
                    installCallback.onInstallLog("File copied\n\n")
                }

                onUI {
                    installCallback.onInstallLog("Connecting to device\n")
                }

                val socket = Socket("localhost", 5555)

                val crypto = AdbCrypto.loadAdbKeyPair({ data -> Base64.encodeToString(data, Base64.NO_WRAP) }, CommonUtils.getKeyPair(App.context))

                val connection = AdbConnection.create(socket, crypto)

                connection.connect()
                onUI {
                    installCallback.onInstallLog("Connected to device\n\n")
                }

                onUI {
                    installCallback.onInstallLog("Copying File $transFilePath to $tgtFilePath\n")
                }

                var stream: AdbStream
                stream = connection.open("shell:" + Constants.COPY_COMMAND.format(transFilePath, tgtFilePath))

                while (!stream.isClosed) {
                    val str: String = try{
                        String(stream.read())
                    } catch (ignored: IOException){
                        ""
                    }
                    for (line in str.split("\\r?\\n").toTypedArray()) {
                        if (line.isNotEmpty()) {
                            onUI {
                                installCallback.onInstallLog(line + "\n")
                            }
                        }
                    }
                }
                onUI {
                    installCallback.onInstallLog("File copied\n\n")
                }

                transFile.delete()

                onUI {
                    installCallback.onInstallLog("Deleted trans file $transFilePath\n\n")
                }

                onUI {
                    installCallback.onInstallLog("Start installing $tgtFilePath\n\n")
                }

                stream = connection.open("shell:" + Constants.INSTALL_COMMAND + tgtFilePath)

                while (!stream.isClosed) {
                    val str: String = try{
                        String(stream.read())
                    } catch (ignored: IOException){
                        ""
                    }
                    for (line in str.split("\\r?\\n").toTypedArray()) {
                        if (line.isNotEmpty()) {
                            onUI {
                                installCallback.onInstallLog(line + "\n")
                            }
                        }
                    }
                }
                onUI {
                    installCallback.onApkInstalled(if(CommonUtils.checkAppInstalled(App.context, apkInfoEntity.packageName)) InstallStatus.SUCCESS else InstallStatus.FAILURE)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onUI {
                    installCallback.onInstallLog("==== Error occurred ====\n")
                    installCallback.onInstallLog("==== StackTrace Start ====\n")
                    installCallback.onInstallLog(e.stackTraceToString())
                    installCallback.onInstallLog("==== StackTrace End ====\n")
                    installCallback.onInstallLog("==== Failed to install the app ====\n")
                    installCallback.onApkInstalled(InstallStatus.FAILURE)
                }
            }
        }
    }

}
