package io.isning.installer.wear.activity.crash

import android.content.ClipData
import android.content.ClipboardManager
import androidx.viewbinding.ViewBinding
import io.isning.installer.wear.R
import io.isning.installer.wear.activity.BaseActivity
import io.isning.installer.wear.databinding.ActivityCrashErrorBinding
import io.isning.installer.wear.utils.lazyBind

class ErrorActivity : BaseActivity() {

    private val vb: ActivityCrashErrorBinding by lazyBind()

    override fun initView(): ViewBinding {
        return vb
    }

    override fun initData() {
        val crashInfo = ActivityOnCrash.getAllErrorDetailsFromIntent(this, intent)
        vb.tvCrashInfo.text = crashInfo
        vb.btnErrorCopy.setOnClickListener {
            copyErrorToClipboard(crashInfo)
            showToast(getString(R.string.copy_error_log))
        }
        vb.btnErrorExit.setOnClickListener {
            ActivityOnCrash.closeApplication(this)
        }
    }

    private fun copyErrorToClipboard(info: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("error", info)
        clipboard.setPrimaryClip(clip)
    }

}