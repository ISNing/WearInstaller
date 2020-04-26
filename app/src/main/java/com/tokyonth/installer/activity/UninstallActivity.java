package com.tokyonth.installer.activity;

import com.tokyonth.installer.Contents;
import com.tokyonth.installer.R;
import com.tokyonth.installer.base.BaseActivity;
import com.tokyonth.installer.widget.CustomizeDialog;
import com.tokyonth.installer.utils.file.ShellUtils;
import com.tokyonth.installer.utils.helper.AppUtils;

import java.util.Objects;

public class UninstallActivity extends BaseActivity {

    @Override
    public int setActivityView() {
        return 0;
    }

    @Override
    public void initActivity() {
        String pkgName = Objects.requireNonNull(getIntent().getDataString()).replace("package:", "");
        CustomizeDialog.getInstance(this)
                .setTitle(R.string.text_uninstall)
                .setMessage(getString(R.string.text_confirm_uninstall_app, AppUtils.getApplicationNameByPackageName(this, pkgName)))
                .setPositiveButton(R.string.text_uninstall, (dialog, which) -> {
                    int result = ShellUtils.execWithRoot(Contents.UNINSTALL_COMMAND + pkgName);
                    String str = result == 0 ? getString(R.string.text_uninstall_complete) : getString(R.string.text_uninstall_failure);
                    showToast(str);
                    finish();
                })
                .setNegativeButton(R.string.dialog_btn_cancel, (dialog, which) -> finish())
                .setCancelable(false).create().show();
    }

}