package com.example.myarchiveapp.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.myarchiveapp.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {
    suspend fun getInstalledUserApps(): List<AppModel> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        
        packages.mapNotNull { packageInfo ->
            val appInfo = packageInfo.applicationInfo
            if (appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                // It's a user app
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val icon = packageManager.getApplicationIcon(appInfo)
                
                // Check if archived (ApplicationInfo.isArchived was added in API 35)
                val isArchived = if (android.os.Build.VERSION.SDK_INT >= 35) {
                    appInfo.isArchived
                } else {
                    false
                }
                
                AppModel(
                    packageName = packageInfo.packageName,
                    appName = appName,
                    icon = icon,
                    isArchived = isArchived
                )
            } else {
                null
            }
        }.sortedBy { it.appName.lowercase() }
    }
}
