package com.ikoro.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("Boot completed; Ikoro service restart deferred to user launch.")
    }
}
