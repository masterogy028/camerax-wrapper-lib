package com.dipl.camerax.utils.scanning_components

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import javax.inject.Inject

class AnalyzeComponent @Inject constructor() {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC
        )
        .build()
    private val scanner = BarcodeScanning.getClient(options)

    fun scanForQRCode(bitmapImage: Bitmap): Task<MutableList<Barcode>> {
        val image = InputImage.fromBitmap(bitmapImage, 0)
        return scanner.process(image)
    }
}
