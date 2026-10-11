package com.example.myarchiveapp.model

import android.graphics.drawable.Drawable

data class AppModel(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isArchived: Boolean
)
