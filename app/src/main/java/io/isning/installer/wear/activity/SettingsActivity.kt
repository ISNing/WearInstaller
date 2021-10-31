package io.isning.installer.wear.activity

import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding

import com.google.android.material.snackbar.Snackbar
import io.isning.installer.wear.App
import io.isning.installer.wear.BuildConfig
import io.isning.installer.wear.R
import io.isning.installer.wear.adapter.SettingsAdapter
import io.isning.installer.wear.adapter.SettingsAdapter.*
import io.isning.installer.wear.data.LocalDataRepo
import io.isning.installer.wear.databinding.ActivitySettingsBinding
import io.isning.installer.wear.utils.FileIOUtils
import io.isning.installer.wear.utils.lazyBind

class SettingsActivity : BaseActivity() {

    private val viewBind: ActivitySettingsBinding by lazyBind()

    private val localDataRepo: LocalDataRepo = App.localData

    private var settingsAdapter: SettingsAdapter? = null

    override fun initView(): ViewBinding {
        initViewStatus()
        return viewBind
    }

    override fun initData() {
        App.localData.setNotFirstBoot()
    }

    private fun initViewStatus() {
        settingsAdapter = SettingsAdapter(this@SettingsActivity).apply {
            setOnItemClickListener(object : OnItemClickListener {
                override fun onSwitch(pos: Int, bool: Boolean) {
                    when (pos) {
                        0 -> localDataRepo.setShowPermission(bool)
                        1 -> localDataRepo.setShowActivity(bool)
                        2 -> localDataRepo.setDefaultSilent(bool)
                        3 -> localDataRepo.setAutoDel(bool)
                        4 -> {
                            if (bool)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            localDataRepo.setFollowSystem(bool)
                        }
                    }
                }

                override fun onClick(pos: Int) {}
            })
        }
        viewBind.rvSettings.apply {
            layoutManager = GridLayoutManager(this@SettingsActivity, 1)
            adapter = settingsAdapter
        }

        val cacheSize = FileIOUtils.byteToString(FileIOUtils.getFileOrFolderSize(externalCacheDir))
        viewBind.tvApkCache.text = getString(R.string.text_apk_cache, cacheSize)
        viewBind.tvVersion.text = getString(R.string.text_settings_version, BuildConfig.VERSION_NAME)
        viewBind.cardApkCache.setOnClickListener {
            FileIOUtils.deleteFolderFile(externalCacheDir?.path, true)
            Snackbar.make(viewBind.rootLayout, getString(R.string.clean_complete), Snackbar.LENGTH_SHORT).show()
            viewBind.tvApkCache.text = getString(R.string.text_apk_cache, "0B")
        }
    }
}
