package io.isning.installer.wear.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import io.isning.installer.wear.App
import io.isning.installer.wear.R
import io.isning.installer.wear.activity.SettingsActivity
import io.isning.installer.wear.databinding.LayoutInstallTopBinding
import io.isning.installer.wear.utils.CommonUtils
import io.isning.installer.wear.utils.lazyBind

class InstallHeadView : FrameLayout {

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private val rootView: LayoutInstallTopBinding by lazyBind()

    private fun initView() {
        nightModeStatus()
        rootView.ibNightMode.setOnClickListener {
            startNightModeFun()
        }
        rootView.ibSettings.setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
        addView(rootView.root)
    }

    fun setAppIcon(icon: Bitmap) {
        rootView.ivAppIcon.setImageBitmap(icon)
        initPaletteColor(icon)
    }

    fun setAppName(name: String) {
        rootView.tvAppName.text = name
    }

    fun setAppVersion(version: String) {
        rootView.tvAppVersion.text = version
    }

    fun showVersionTip(versionCode: Int, installedVersionCode: Int) {
        rootView.tvVersionTips.apply {
            val translateAnimation = TranslateAnimation(0f, 0f, -200f, 0f)
            translateAnimation.duration = 500
            alpha = 0.70f
            animation = translateAnimation
            startAnimation(translateAnimation)
            visibility = View.VISIBLE
            text = CommonUtils.checkVersion(context, versionCode, installedVersionCode)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        //super.setEnabled(enabled)
        rootView.ibNightMode.isEnabled = enabled
        rootView.ibSettings.isEnabled = enabled
    }

    private fun nightModeStatus() {
        App.localData.isNightMode().let {
            val res = if (it) {
                R.drawable.ic_night
            } else {
                R.drawable.ic_dark
            }
            rootView.ibNightMode.setImageResource(res)
        }
    }

    private fun initPaletteColor(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            val vibrantSwatch = palette!!.lightVibrantSwatch
            val color: Int = if (vibrantSwatch != null) {
                CommonUtils.colorBurn(vibrantSwatch.rgb)
            } else {
                ContextCompat.getColor(context, R.color.colorAccent)
            }
            val drawableHead = ContextCompat.getDrawable(context, R.drawable.bg_round_8)?.mutate()
            drawableHead?.setTint(color)

            rootView.tvVersionTips.setTextColor(color)
            rootView.csHead.let {
                val width = it.measuredWidth
                val height = it.measuredHeight
                ViewAnimationUtils.createCircularReveal(it, width / 2, height / 2, 0f, height.toFloat()).apply {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            super.onAnimationStart(animation)
                            it.background = drawableHead
                        }
                    })
                    duration = 500
                    start()
                }
            }
        }
    }

    private fun startNightModeFun() {
        App.localData.isNightMode().let {
            if (it) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            App.localData.setNightMode(!it)
        }
        nightModeStatus()
    }

}
