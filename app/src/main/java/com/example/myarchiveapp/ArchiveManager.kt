package com.example.myarchiveapp

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import androidx.annotation.RequiresApi

class ArchiveManager(private val context: Context) {
    
    var onArchiveSuccess: ((String) -> Unit)? = null

    companion object {
        const val ACTION_ARCHIVE_STATUS = "com.example.myarchiveapp.ACTION_ARCHIVE_STATUS"
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_ARCHIVE_STATUS) {
                val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                
                if (status == PackageInstaller.STATUS_SUCCESS && packageName != null) {
                    onArchiveSuccess?.invoke(packageName)
                }
                
                if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                    val confirmationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_INTENT)
                    }
                    if (confirmationIntent != null) {
                        confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(confirmationIntent)
                    }
                }
            }
        }
    }

    fun register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(ACTION_ARCHIVE_STATUS), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(ACTION_ARCHIVE_STATUS))
        }
    }

    fun unregister() {
        context.unregisterReceiver(receiver)
    }

    @RequiresApi(35)
    fun requestArchive(packageName: String) {
        try {
            val packageInstaller = context.packageManager.packageInstaller
            val intent = Intent(ACTION_ARCHIVE_STATUS).apply {
                setPackage(context.packageName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                packageName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            packageInstaller.requestArchive(packageName, pendingIntent.intentSender)
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            android.util.Log.e("ArchiveManager", "App cannot be archived (no installer found)", e)
            android.widget.Toast.makeText(context, "App cannot be archived (no installer found)", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.util.Log.e("ArchiveManager", "Error archiving app", e)
        }
    }
}
