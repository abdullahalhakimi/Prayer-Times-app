package com.prayertimesApp.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_BOOT_COMPLETED == action || Intent.ACTION_MY_PACKAGE_REPLACED == action) {
            Log.d(TAG, "Device rebooted or package replaced ($action). Re-enabling alarms if active.")
            // On system startup, alarm manager exact alarms are cleared.
            // Future alarm instances or scheduled reminders can be re-registered here.
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
