package fr.phytok.apps.youcast

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class Service : Service() {
    override fun onBind(intent: Intent): IBinder? {
        Log.i("BIDON", "Service received intent")
        return null
    }
}