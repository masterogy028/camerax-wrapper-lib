package com.dipl.camerax.utils

import android.content.Context
import com.dipl.camerax.R
import java.io.File

/** Use external media if it is available, our app's file directory otherwise */
fun Context.getOutputDirectory(): File {
    val appContext = applicationContext
    val mediaDir = externalMediaDirs.firstOrNull()?.let {
        File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else appContext.filesDir
}
