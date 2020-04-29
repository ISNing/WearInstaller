package com.tokyonth.installer.utils;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tokyonth.installer.Contents;
import com.tokyonth.installer.R;
import com.tokyonth.installer.utils.SPUtils;
import com.tokyonth.installer.widget.CustomizeDialog;

public class VersionHelper {

    public static String CheckVer(Context context, int version, int installedVersion) {
        if (version == installedVersion) {
            return context.getString(R.string.text_equal_ver);
        } else if (version > installedVersion) {
            return context.getString(R.string.text_new_ver);
        } else {
            CustomizeDialog.getInstance(context)
                    .setTitle(R.string.dialog_title_tips)
                    .setMessage(R.string.low_ver_msg)
                    .setPositiveButton(R.string.text_i_know, (dialog, which) -> SPUtils.putData(Contents.SP_NO_TIP_VERSION, false))
                    .setNegativeButton(R.string.dialog_no_longer_prompt, null)
                    .setCancelable(false).create().show();
            return context.getString(R.string.text_low_ver);
        }
    }

}
