package io.isning.installer.wear.install

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat.startActivity
import io.isning.installer.wear.App

class UninstallTask(private val pkgName: String,
                    private val uninstallCallback: UninstallCallback) : Thread() {

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun run() {
        super.run()
        val uri= Uri.parse("package:$pkgName")
        val intent = Intent(Intent.ACTION_DELETE, uri)
        startActivity(App.context, intent, null)
        // TODO: Check and remove this
        handler.post { uninstallCallback.onUninstallResult(0) }
    }
}
