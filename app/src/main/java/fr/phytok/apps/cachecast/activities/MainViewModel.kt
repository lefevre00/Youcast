package fr.phytok.apps.cachecast.activities

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.phytok.apps.cachecast.LocalTrackRepository
import fr.phytok.apps.cachecast.model.Track
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject constructor(
        private val localTrackRepository: LocalTrackRepository
    ) : ViewModel() {

    val loading = mutableStateOf(true)

    private val myTracks = mutableStateListOf<Track>()

    fun loadTracks() {
        Executors.newScheduledThreadPool(1) // schedule another request for 2 seconds later
            .schedule({
                Log.d(TAG, "Start loading")
                myTracks.clear()
                myTracks.addAll(localTrackRepository.searchTrack())
                Log.d(TAG, "Found ${myTracks.size} track(s)")
                loading.value = false
                Log.d(TAG, "Finish loading")
        }, 2, TimeUnit.SECONDS)
    }

    fun getTracks(): List<Track> = myTracks

    companion object {
        private const val TAG = "MainViewModel"
    }
}