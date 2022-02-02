package fr.phytok.apps.cachecast.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class PermissionService
@Inject constructor(
    @ActivityContext private val context: Context
) {

    fun canReadStorage(): Boolean = hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

    fun canWriteStorage(): Boolean = hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED

    private fun doRequestPermission(activity: Activity, permission: String) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            REQUEST_CODES_BY_PERMISSION.getValue(permission)
        )
    }

    fun askReadStorage(activity: Activity) {
        doRequestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun askWriteStorage(activity: Activity) {
        // Todo handle permission refused with message to user
        doRequestPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun handleResponse(requestCode: Int, grantResults: IntArray, onGranted: () -> Unit) {
        if (isRequestCodeManaged(requestCode)) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted")
                onGranted()
            } else {
                Log.d(TAG, "Permission refused")
            }
        } else {
            Log.d(TAG, "Request code not handle $requestCode")
        }
    }

    private fun isRequestCodeManaged(code: Int): Boolean =
        REQUEST_CODES_BY_PERMISSION.containsValue(code)

    companion object {
        const val TAG = "PermissionService"
        val REQUEST_CODES_BY_PERMISSION = mapOf(
            Manifest.permission.READ_EXTERNAL_STORAGE to 0x1045,
            Manifest.permission.WRITE_EXTERNAL_STORAGE to 0x1046,
        )
    }
}