package fr.phytok.apps.cachecast.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import dagger.hilt.android.qualifiers.ActivityContext
import fr.phytok.apps.cachecast.activities.MainActivity
import javax.inject.Inject

class PermissionService
@Inject constructor(
    @ActivityContext private val context: Context
) {

    fun canReadStorage(): Boolean = hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED

    fun askReadStorage(activity: Activity) {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
//            ,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(activity, permissions, READ_EXTERNAL_STORAGE_REQUEST
        )
    }

    fun handleResponse(requestCode: Int, grantResults: IntArray, onGranted: () -> Unit) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    Log.d(MainActivity.TAG, "Permission granted")
                    onGranted()
                } else {
                    Log.d(MainActivity.TAG, "Permission refused")
                }
            }
        }
    }

    companion object {
        /** The request code for requesting [Manifest.permission.READ_EXTERNAL_STORAGE] permission. */
        const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045
    }
}