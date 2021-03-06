package io.isning.installer.wear.data

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import io.isning.installer.wear.Constants

import java.util.ArrayList

class ApkInfoEntity() : Parcelable {

    //var icon: Bitmap? = null
    var filePath: String? = null
    var fileUri: Uri? = null
    var uriReferrer: String? = null
    var appName: String? = null
    var versionName: String? = null
    var versionCode: Int = 0
    var packageName: String? = null
    var isHasInstalledApp: Boolean = false
    var installedVersionName: String? = null
    var installedVersionCode: Int = 0

    var permissions: Array<String>? = null
    var activities: Array<ActivityInfo>? = null
    var permissionsDesc: Triple<ArrayList<String>, ArrayList<String>, ArrayList<String>>? = null

    val version: String
        get() = "$versionName($versionCode)"

    val installedVersion: String
        get() = if (isHasInstalledApp) "$installedVersionName($installedVersionCode)" else " -- "

    fun hasInstalledApp(): Boolean {
        return isHasInstalledApp
    }

    fun getIcon(): Bitmap? {
        return Constants.APP_ICON_BITMAP
    }

    fun setIcon(bitmap: Bitmap) {
        Constants.APP_ICON_BITMAP = bitmap
    }

    constructor(parcel: Parcel) : this() {
        //icon = parcel.readParcelable(Bitmap::class.java.classLoader)
        filePath = parcel.readString()
        fileUri = parcel.readParcelable(Uri::class.java.classLoader)
        uriReferrer = parcel.readString()
        appName = parcel.readString()
        versionName = parcel.readString()
        versionCode = parcel.readInt()
        packageName = parcel.readString()
        isHasInstalledApp = parcel.readByte() != 0.toByte()
        installedVersionName = parcel.readString()
        installedVersionCode = parcel.readInt()
        //permissions = parcel.createStringArray()
        //activities = parcel.createTypedArrayList(ActivityInfo.CREATOR)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        //parcel.writeParcelable(icon, flags)
        parcel.writeString(filePath)
        parcel.writeParcelable(fileUri, flags)
        parcel.writeString(uriReferrer)
        parcel.writeString(appName)
        parcel.writeString(versionName)
        parcel.writeInt(versionCode)
        parcel.writeString(packageName)
        parcel.writeByte(if (isHasInstalledApp) 1 else 0)
        parcel.writeString(installedVersionName)
        parcel.writeInt(installedVersionCode)
        //parcel.writeStringArray(permissions)
        //parcel.writeTypedList(activities)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ApkInfoEntity> {
        override fun createFromParcel(parcel: Parcel): ApkInfoEntity {
            return ApkInfoEntity(parcel)
        }

        override fun newArray(size: Int): Array<ApkInfoEntity?> {
            return arrayOfNulls(size)
        }
    }

}
