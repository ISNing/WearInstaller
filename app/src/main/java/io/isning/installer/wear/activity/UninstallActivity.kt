package io.isning.installer.wear.activity

import androidx.viewbinding.ViewBinding
import io.isning.installer.wear.R
import io.isning.installer.wear.install.UninstallCallback
import io.isning.installer.wear.install.UninstallTask
import io.isning.installer.wear.utils.PackageUtils
import io.isning.installer.wear.view.CustomizeDialog
import java.util.regex.Matcher
import java.util.regex.Pattern

class UninstallActivity : BaseActivity(), UninstallCallback {

    private lateinit var uninstallTask: UninstallTask
    private lateinit var pkgName: String

    override fun initView(): ViewBinding? {
        return null
    }

    override fun initData() {
        intent.dataString?.let {
            val pattern: Pattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)+([.][a-zA-Z_][a-zA-Z0-9_]*)+")
            val matcher: Matcher = pattern.matcher(it)
            while (matcher.find()) {
                pkgName = matcher.group()
            }
        }

        val appName = PackageUtils.getAppNameByPackageName(this, pkgName)
        //val uninstallMode = get(Constants.SP_INSTALL_MODE, 0)
        uninstallTask = UninstallTask(pkgName, this)

        if (pkgName.isNotEmpty()) {
            CustomizeDialog.getInstance(this)
                    .setMessage(getString(R.string.text_confirm_uninstall_app, appName))
                    .setPositiveButton(R.string.apk_uninstall) { _, _ ->
                        uninstallTask.start()
                    }
                    .setNegativeButton(R.string.dialog_btn_cancel) { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
        } else {
            showToast(getString(R.string.cannot_found_pkg))
        }
    }

    override fun onUninstallResult(resultCode: Int) {
        //TODO: Check and remove this
//        showToast(
//                if (resultCode == 0)
//                    getString(R.string.text_uninstall_complete)
//                else
//                    getString(R.string.text_uninstall_failure)
//        )
        finish()
    }

}
