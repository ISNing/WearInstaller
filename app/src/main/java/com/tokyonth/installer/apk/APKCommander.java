package com.tokyonth.installer.apk;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AndroidRuntimeException;

import com.tokyonth.installer.Config;
import com.tokyonth.installer.bean.ApkInfo;
import com.tokyonth.installer.helper.ContentUriUtils;
import com.tokyonth.installer.permissions.PermInfo;
import com.tokyonth.installer.helper.PathUtils;
import com.tokyonth.installer.utils.ShellUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class APKCommander {

    private Context context;
    private Uri uri;
    private ApkInfo mApkInfo;
    private ICommanderCallback callback;
    private Handler handler;
    private PermInfo info;

    private String source_app;

    public APKCommander(Context context, Uri uri, ICommanderCallback commanderCallback, String source_app) {
        this.context = context;
        this.uri = uri;
        this.callback = commanderCallback;
        this.source_app = source_app;
        handler = new Handler(Looper.getMainLooper());
        new ParseApkTask().start();
    }

    public void startInstall() {
        new InstallApkTask().start();
    }

    public ApkInfo getApkInfo() {
        return mApkInfo;
    }

    public PermInfo getInfo() {
        return info;
    }

    private class InstallApkTask extends Thread {
        @Override
        public void run() {
            super.run();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onApkPreInstall(mApkInfo);
                }
            });

            if (Build.VERSION.SDK_INT >= 24) {
                ShellUtils.execWithRoot(Config.SELINUX_COMMAND);
            }

            final int retCode = ShellUtils.execWithRoot(Config.INSTALL_COMMAND + "\"" + mApkInfo.getApkFile().getPath() + "\"" + "\n", new ShellUtils.Result() {
                @Override
                public void onStdout(final String text) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onInstallLog(mApkInfo, text);
                        }
                    });
                }

                @Override
                public void onStderr(final String text) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onInstallLog(mApkInfo, text);
                        }
                    });
                }

                @Override
                public void onCommand(String command) {

                }

                @Override
                public void onFinish(int resultCode) {

                }
            });
            if (retCode == 0 && mApkInfo.isFakePath()) {
                mApkInfo.getApkFile().delete();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onApkInstalled(mApkInfo, retCode);
                }
            });
        }
    }

    private class ParseApkTask extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStartParseApk(uri);
                    }
                });
                mApkInfo = new ApkInfo();
                info = new PermInfo();

                String classification = ContentUriUtils.getPath(context, uri);
                String ordinary = PathUtils.getRealFilePath(context, uri);
                String apkSourcePath = (classification == null) ? ordinary : classification;

                assert apkSourcePath != null;
                PackageManager pms = context.getPackageManager();
                String appVersion = null;
                String appCode = null;

                try {
                    PackageInfo pi = pms.getPackageInfo(Config.PKG_NAME, 0);
                    appVersion = pi.versionName;
                    appCode = String.valueOf(pi.versionCode);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (apkSourcePath.contains(Config.PKG_NAME)) {
                    apkSourcePath = apkSourcePath.replace(Config.PKG_NAME, source_app);
                    if (apkSourcePath.contains(source_app + "-" + appVersion + "-" + appCode)) {
                        apkSourcePath = apkSourcePath.replace(source_app + "-" + appVersion + "-" + appCode,
                                Config.PKG_NAME + "-" + appVersion + "-" + appCode);
                    }
                }
                if (apkSourcePath.contains(Config.DATA_PATH_PERFIX)) {
                    mApkInfo.setFakePath(true);
                    File tempFile = new File(context.getExternalCacheDir(), System.currentTimeMillis() + Config.APK_POSTFIX);
                    try {
                        InputStream is = context.getContentResolver().openInputStream(uri);
                        if (is != null) {
                            OutputStream fos = new FileOutputStream(tempFile);
                            byte[] buf = new byte[4096 * 1024];
                            int ret;
                            while ((ret = is.read(buf)) != -1) {
                                fos.write(buf, 0, ret);
                                fos.flush();
                            }
                            fos.close();
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    apkSourcePath = tempFile.getPath();
                }
                mApkInfo.setApkFile(new File(apkSourcePath));

                List<String> act = new ArrayList<>();
                PackageManager pm = context.getPackageManager();
                PackageInfo pkgInfo = pm.getPackageArchiveInfo(mApkInfo.getApkFile().getPath(), PackageManager.GET_PERMISSIONS);
                if (pkgInfo != null) {
                    pkgInfo.applicationInfo.sourceDir = mApkInfo.getApkFile().getPath();
                    pkgInfo.applicationInfo.publicSourceDir = mApkInfo.getApkFile().getPath();
                    mApkInfo.setAppName(pm.getApplicationLabel(pkgInfo.applicationInfo).toString());
                    mApkInfo.setPackageName(pkgInfo.applicationInfo.packageName);
                    mApkInfo.setVersionName(pkgInfo.versionName);
                    mApkInfo.setVersionCode(pkgInfo.versionCode);
                    mApkInfo.setIcon(pkgInfo.applicationInfo.loadIcon(pm));

                    PackageInfo act_packageInfo = pm.getPackageArchiveInfo(mApkInfo.getApkFile().getPath(), PackageManager.GET_ACTIVITIES);
                    for (ActivityInfo activity : act_packageInfo.activities) {
                        act.add(activity.name);
                    }
                    mApkInfo.setActivities(act);

                    try {
                        PackageInfo installedPkgInfo = pm.getPackageInfo(mApkInfo.getPackageName(), 0);
                        mApkInfo.setInstalledVersionName(installedPkgInfo.versionName);
                        mApkInfo.setInstalledVersionCode(installedPkgInfo.versionCode);
                        mApkInfo.setHasInstalledApp(true);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        mApkInfo.setHasInstalledApp(false);
                    }

                    mApkInfo.setPermissions(pkgInfo.requestedPermissions);
                    List<String> str_list = new ArrayList<>();
                    if (pkgInfo.requestedPermissions != null) {
                        Collections.addAll(str_list, pkgInfo.requestedPermissions);
                        getPermissionInfo(str_list);
                    }

                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onApkParsed(mApkInfo);
                    }
                });
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onApkParsed(null);
                    }
                });
                e.printStackTrace();
                throw new AndroidRuntimeException(e);
            }
        }
    }

    private void getPermissionInfo(List<String> permission) {

        List<String> Group = new ArrayList<>();
        List<String> Label = new ArrayList<>();
        List<String> Description = new ArrayList<>();

        for (String str : permission) {
            try {
                PackageManager packageManager = context.getPackageManager();
                PermissionInfo permissionInfo = packageManager.getPermissionInfo(str, 0);

                PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
                Group.add(permissionGroupInfo.loadLabel(packageManager).toString());

                String permissionLabel = permissionInfo.loadLabel(packageManager).toString();
                Label.add(permissionLabel);

                String permissionDescription = permissionInfo.loadDescription(packageManager).toString();
                Description.add(permissionDescription);

            } catch (PackageManager.NameNotFoundException e) {
                Description.add("");
            }
        }
        info.setPermissionDescription(Description);
        info.setPermissionGroup(Group);
        info.setPermissionLabel(Label);
    }

}
