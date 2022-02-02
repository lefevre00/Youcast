package fr.phytok.apps.cachecast.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import fr.phytok.apps.cachecast.model.TrackDto
import fr.phytok.apps.cachecast.services.DownloadService
import fr.phytok.apps.cachecast.services.PermissionService
import javax.inject.Inject


@AndroidEntryPoint
class ShareUrlActivity : AppCompatActivity() {

    private val myViewModel: ShareViewModel by viewModels()

    @Inject
    lateinit var permissionService: PermissionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrawScreen()
        }

        onStorageWritable {
            handleAction()
        }
    }

    private fun handleAction() {
        if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { url ->
                myViewModel.tryToLoadUrl(url) { result ->
                    if (result.inCache) {
                        Log.d(TAG, "Track already known")
                    } else {
                        result.trackDto?.let { askForDownload(it) }
                    }
                }
            } ?: run {
                Log.d(TAG, "No extra text received")
            }
        } else {
            Log.w(TAG, "Unhandled intent action: ${intent?.action}")
        }
        finish()
    }

    @Composable
    fun DrawScreen() {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = TAG, style = MaterialTheme.typography.h4)

            val url by remember { myViewModel.askedUrl }
            val text = if (url.isEmpty()) "" else "You're looking for $url..."
            Text(text = text, style = MaterialTheme.typography.subtitle2)

            Spacer(Modifier.size(size = 20.dp))

            val status by remember { myViewModel.requestStatus }
            Text(text = status, style = MaterialTheme.typography.h5)
        }
    }

    private fun onStorageWritable(block: () -> Unit) {
        if (permissionService.canWriteStorage()) {
            block()
        } else {
            permissionService.askWriteStorage(this)
        }
    }

    private fun askForDownload(track: TrackDto) {
        Intent(this, DownloadService::class.java).also { newIntent ->
            newIntent.action = DownloadService.ACTION_LOAD
            newIntent.putExtra(ShareViewModel.EXTRA_ID, track.id)
            startService(newIntent)
        }
    }

    companion object {
        const val EXTRA_KEY = "fr.phytok.apps.cachecast.extra.key"
        private const val TAG = "ShareUrlActivity"
    }
}