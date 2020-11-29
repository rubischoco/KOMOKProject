package com.teamC.komok.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class PermissionUtils {
    // https://github.com/rakaadinugroho/ImageCropperMLKit/blob/master/app/src/main/java/com/rakaadinugroho/imagefacecroper/PermissionUtils.java
    fun requestPermission(activity: Activity?, requestCode: Int, vararg permissions: String): Boolean {
        var granted = true
        val permissionsNeeded = ArrayList<String>()

        for (s in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(activity!!, s)
            val hasPermission = permissionCheck == PackageManager.PERMISSION_GRANTED
            granted = granted and hasPermission
            if (!hasPermission) {
                permissionsNeeded.add(s)
            }
        }

        return if (granted) { true }
        else {
            ActivityCompat.requestPermissions(activity!!, permissionsNeeded.toTypedArray(), requestCode)
            false
        }
    }


    fun permissionGranted(requestCode: Int, permissionCode: Int, grantResults: IntArray): Boolean {
        return if (requestCode == permissionCode) {
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else false
    }
}