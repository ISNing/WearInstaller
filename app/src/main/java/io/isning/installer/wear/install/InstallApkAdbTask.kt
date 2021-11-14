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
import java.util.*

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
                    installCallback.onInstallLog("Copying File $curFilePath to $tgtFilePath\n")
                }

                copyFileAdb(connection, curFilePath, tgtFilePath, object: TaskCallback {
                    override fun onSuccess() {
                        onUI {
                            installCallback.onInstallLog("File copied\n\n")
                        }
                    }
                    override fun onFailure() {
                        // The adb environment might has no permission to access to /data/ or some other dictionaries.
                        // So, we copy the file waiting to be installed to the root of internal storage,
                        // then use adb to copy the transfer file(which is located in internal storage) to /data/tmp to install it.
                        // (Why /data/tmp? Because it is not permitted to install file in the internal storage.
                        // If you do, you will get only "EACCESS")

                        // Copy source file to transfer file
                        onUI {
                            installCallback.onInstallLog("File copy failed, trying to deal with this by transferring file to $transFilePath\n\n")
                            installCallback.onInstallLog("Copying File $curFilePath to $transFilePath for transferring\n")
                        }
                        copyFileAndroid(fromFile, transFile, object : TaskCallback{
                            override fun onSuccess() {
                                onUI {
                                    installCallback.onInstallLog("File copied\n\n")
                                }
                            }

                            override fun onFailure() {
                                onUI {
                                    installCallback.onInstallLog("File copy failed, check logs before for details\n\n")
                                    installCallback.onApkInstalled(InstallStatus.FAILURE)
                                }
                            }

                            override fun onFailure(e: Exception) {
                                onUI {
                                    installCallback.onInstallLog("File copy failed, throwing exception\n\n")
                                    throw e
                                }
                            }
                        })

                        // Copy transfer file to target file
                        onUI {
                            installCallback.onInstallLog("Copying File $transFilePath to $tgtFilePath\n\n")
                        }
                        copyFileAdb(connection, transFilePath, tgtFilePath, object : TaskCallback{
                            override fun onSuccess() {
                                onUI {
                                    installCallback.onInstallLog("File copied\n\n")
                                }
                            }

                            override fun onFailure() {
                                onUI {
                                    installCallback.onInstallLog("File copy failed, check logs before for details\n\n")
                                    installCallback.onApkInstalled(InstallStatus.FAILURE)
                                }
                            }

                            override fun onFailure(e: Exception) {
                                onUI {
                                    installCallback.onInstallLog("File copy failed, throwing exception\n\n")
                                    throw e
                                }
                            }

                        })

                        // Delete transfer file
                        onUI {
                            installCallback.onInstallLog("Deleting trans file $transFilePath\n")
                        }
                        transFile.delete()
                        onUI {
                            installCallback.onInstallLog("Deleted trans file $transFilePath\n\n")
                        }
                    }
                    override fun onFailure(e: Exception) {

                    }
                })


                onUI {
                    installCallback.onInstallLog("Start installing $tgtFilePath\n\n")
                }
                installApkAdb(connection, tgtFilePath, object : TaskCallback {
                    override fun onSuccess() {
                        onUI {
                            installCallback.onApkInstalled(InstallStatus.SUCCESS)
                        }
                    }

                    override fun onFailure() {
                        onUI {
                            installCallback.onInstallLog("File install failed, check logs before for details\n\n")
                            installCallback.onApkInstalled(InstallStatus.FAILURE)
                        }
                    }

                    override fun onFailure(e: Exception) {
                        onUI {
                            installCallback.onInstallLog("File install failed, throwing exception\n\n")
                            throw e
                        }
                    }
                })

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
    data class ProcessTags(val tag: String=UUID.randomUUID().toString()) {
        fun getTaggedScript(rawScript: String): String {
            return  "$rawScript;if [ \$? -eq 0 ];then echo \"\\n${getSuccessTag()}\";else echo \"\\n${getFailureTag()}\";fi;"
        }
        fun getSuccessTag(): String{
            return "< Success $tag >"
        }
        fun getFailureTag(): String{
            return "< Failure $tag >"
        }
    }

    private fun startWatchingStream(stream: AdbStream, tags: ProcessTags, cb: TaskCallback) {
        while (!stream.isClosed) {
            val str: String = try{
                String(stream.read())
            } catch (ignored: IOException){
                ""
            }
            for (line in str.split("\\r?\\n").toTypedArray()) {
                if (line.isNotEmpty()) {
                    val s = line.replace("\n","").replace("\r","")
                    onUI {
                        installCallback.onInstallLog(s + "\n")
                    }
                    if(s == tags.getSuccessTag()) {
                        stream.close()
                        cb.onSuccess()
                    }else if(s == tags.getFailureTag()) {
                        stream.close()
                        cb.onFailure()
                    }
                }
            }
        }
    }

    fun installApkAdb(connection: AdbConnection, path: String, cb:TaskCallback){
        try {
            val tags = ProcessTags()
            val stream = connection.open("shell:" + tags.getTaggedScript(Constants.INSTALL_COMMAND.format(path)))
            startWatchingStream(stream, tags, cb)
        }catch (e: Exception) {
            cb.onFailure(e)
        }
    }

    fun copyFileAdb(connection: AdbConnection, fromPath: String, toPath: String, cb: TaskCallback){
        try {
            val tags = ProcessTags()
            val stream: AdbStream = connection.open("shell:" + tags.getTaggedScript(Constants.COPY_COMMAND.format(fromPath, toPath)))
            startWatchingStream(stream, tags, cb)
        }catch (e: Exception) {
            cb.onFailure(e)
        }
    }

    fun copyFileAndroid(from: File, to: File, cb: TaskCallback){
        try {
            FileIOUtils.copyFile(from, to, true)
        }catch (e: Exception) {
            cb.onFailure(e)
        }
    }

    interface TaskCallback {
        fun onSuccess()
        fun onFailure()
        fun onFailure(e: Exception)
    }
}
