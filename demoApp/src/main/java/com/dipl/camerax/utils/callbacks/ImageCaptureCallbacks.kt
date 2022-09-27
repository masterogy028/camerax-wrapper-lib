package com.dipl.camerax.utils.callbacks

import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageProxy
import com.dipl.cameraxlib.usecase.image_capture.ImageCapturedCallback
import com.dipl.cameraxlib.usecase.image_capture.ImageSavedCallback

object ImageCaptureCallbackImpl : ImageCapturedCallback {
    override fun onSuccess(image: ImageProxy) {
        Log.d("ImageCaptureSuccess", "onSuccess: ${image.imageInfo}")
    }
}

object ImageSavedCallbackImpl : ImageSavedCallback {
    override fun onSuccess(uri: Uri) {
        Log.d("ImageSavedSuccess", "onSuccess: ${uri.path}")
    }
}
