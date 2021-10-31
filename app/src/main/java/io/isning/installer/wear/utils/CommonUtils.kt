package io.isning.installer.wear.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.isning.installer.wear.App
import io.isning.installer.wear.Constants
import io.isning.installer.wear.R
import io.isning.installer.wear.utils.SPUtils.get
import io.isning.installer.wear.utils.SPUtils.set
import io.isning.installer.wear.view.CustomizeDialog
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Enumeration
import kotlin.math.floor

import android.text.TextUtils
import android.util.Log


object CommonUtils {

    fun checkVersion(context: Context, version: Int, installedVersion: Int): String {
        return when {
            version == installedVersion -> {
                context.getString(R.string.apk_equal_version)
            }
            version > installedVersion -> {
                context.getString(R.string.apk_new_version)
            }
            else -> {
                if (!App.localData.isNeverShowTip()) {
                    CustomizeDialog.getInstance(context)
                            .setTitle(R.string.dialog_title_tip)
                            .setMessage(R.string.low_version_tip)
                            .setPositiveButton(R.string.dialog_btn_ok, null)
                            .setNegativeButton(R.string.dialog_no_longer_prompt) { _, _ ->
                                App.localData.setNeverShowTip()
                            }
                            .setCancelable(false)
                            .show()
                }
                context.getString(R.string.apk_low_version)
            }
        }
    }

    fun toSelfSetting(context: Context, str: String?) {
        val intent = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            data = Uri.fromParts("package", str, null)
        }
        context.startActivity(intent)
    }

    fun colorBurn(RGBValues: Int): Int {
        var red = RGBValues shr 16 and 0xFF
        var green = RGBValues shr 8 and 0xFF
        var blue = RGBValues and 0xFF
        red = floor(red * (1 - 0.1)).toInt()
        green = floor(green * (1 - 0.1)).toInt()
        blue = floor(blue * (1 - 0.1)).toInt()
        return Color.rgb(red, green, blue)
    }

    @Suppress("DEPRECATION")
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        val config = if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        val bitmap = Bitmap.createBitmap(w, h, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    fun getBitmapFromDrawable(context: Context?, @DrawableRes drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawable || drawable is VectorDrawableCompat) {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }

    /**
     * 获取准确的Intent Referrer
     */
    fun reflectGetReferrer(context: Context?): String? {
        return try {
            val activityClass = Class.forName("android.app.Activity")
            val refererField = activityClass.getDeclaredField("mReferrer")
            refererField.isAccessible = true
            refererField[context] as String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getIpAddressString(): String {
        try {
            val enNetI: Enumeration<NetworkInterface> = NetworkInterface
                .getNetworkInterfaces()
            while (enNetI.hasMoreElements()) {
                val netI: NetworkInterface = enNetI.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = netI
                    .inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return "0.0.0.0"
    }

    fun getKeyPair(context: Context): KeyPair {
        val publicKeyBase64 = context.get(Constants.KEY_ADB_KEYPAIR_PUB, "")
        val privateKeyBase64 = context.get(Constants.KEY_ADB_KEYPAIR_PRI, "")

        val keyPair: KeyPair
        if(publicKeyBase64.isEmpty() || privateKeyBase64.isEmpty()) {
            Log.w("CommonUtils", "KeyPair not found, generating a new one")
            keyPair = newKeyPair()
            setKeyPair(context, keyPair)
        }else {
            keyPair = base642KeyPair(publicKeyBase64, privateKeyBase64)
        }
        return keyPair
    }

    fun base642KeyPair(pub: String, pri: String): KeyPair {
        val publicKeyEncoded = Base64.decode(pub, Base64.DEFAULT)
        val privateKeyEncoded = Base64.decode(pri, Base64.DEFAULT)
        val publicKey = KeyFactory.getInstance(Constants.KEY_ALGORITHM).generatePublic(X509EncodedKeySpec(publicKeyEncoded))
        val privateKey = KeyFactory.getInstance(Constants.KEY_ALGORITHM).generatePrivate(PKCS8EncodedKeySpec(privateKeyEncoded))
        return KeyPair(publicKey, privateKey)
    }

    fun keyPair2Base64(keyPair: KeyPair): Pair<String, String>{
        val publicKey: PublicKey = keyPair.public
        val privateKey: PrivateKey = keyPair.private

        val publicKeyBase64 = Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
        val privateKeyBase64 = Base64.encodeToString(privateKey.encoded, Base64.DEFAULT)
        return Pair(publicKeyBase64.toString(), privateKeyBase64.toString())
    }


    fun newKeyPair(): KeyPair {
        return KeyPairGenerator.getInstance(Constants.KEY_ALGORITHM).genKeyPair()
    }

    fun setKeyPair(context: Context, keyPair: KeyPair) {
        val (publicKeyBase64, privateKeyBase64) = keyPair2Base64(keyPair)

        context[Constants.KEY_ADB_KEYPAIR_PUB] = publicKeyBase64
        context[Constants.KEY_ADB_KEYPAIR_PRI] = privateKeyBase64
    }

    fun checkAppInstalled(context: Context, packageName: String?): Boolean {
        return if (TextUtils.isEmpty(packageName)) false else try {
            context.packageManager
                    .getApplicationInfo(packageName!!,
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) PackageManager.MATCH_UNINSTALLED_PACKAGES else PackageManager.GET_UNINSTALLED_PACKAGES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
