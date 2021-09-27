package com.dipl.camerax

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageCapture
import androidx.core.view.drawToBitmap
import androidx.navigation.fragment.findNavController
import com.dipl.camerax.databinding.FragmentCameraXSummaryBinding
import com.dipl.camerax.utils.*
import com.dipl.cameraxlib.CameraXController
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeImageListener
import com.dipl.cameraxlib.usecase.image_analysis.ImageCrop
import com.dipl.cameraxlib.usecase.image_analysis.OBScannerType
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.BarcodeScannerResultListener
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.FaceRecognitionScannerResultListener
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.QRScannerResultListener
import com.dipl.cameraxlib.usecase.image_analysis.createOBImageAnalysis
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureCallbacks
import com.dipl.cameraxlib.usecase.image_capture.OBImageCapture
import com.dipl.cameraxlib.usecase.image_capture.createOBImageCapture
import com.dipl.cameraxlib.usecase.preview.createOBPreview
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import java.io.File

class CameraXSummaryFragment :
    ViewBindingFragment<FragmentCameraXSummaryBinding>(bindViewBy = {
        FragmentCameraXSummaryBinding.inflate(it)
    }), ImageCaptureCallbacks {

    var requestStorageActivityLauncher: ActivityResultLauncher<String>? = null

    private var controller: CameraXController? = null
    private lateinit var obImageCapture: OBImageCapture
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var scanType: DomainScanType = DomainScanType.BarcodeScanner
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC
        )
        .build()
    private val scanner = BarcodeScanning.getClient(options)
    private val targetBitmap by lazy { viewBinding.ivRectView.drawToBitmap() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStorageActivityLauncher = registerActivityResultForCameraPermission {
            setUpCameraX()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermissions()
        viewBinding.tvScannerMode.text =
            getString(R.string.current_mode, scanType.toString())
        viewBinding.btnTakePicture.setOnClickListener {
            obImageCapture.takePicture(
                "cameraX_dipl${System.currentTimeMillis()}",
                ".png",
                requireContext().getOutputDirectory(),
                requireContext()
            )
        }
        viewBinding.btnSwapCamera.setOnClickListener {
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            setUpCameraX()
        }
        viewBinding.btnScanner.setOnClickListener {
            scanType = scanType.nextType()
            viewBinding.tvScannerMode.text =
                getString(R.string.current_mode, scanType.toString())
            setUpCameraX()
        }
    }

    private fun checkCameraPermissions() {
        if (requestStorageActivityLauncher != null) {
            requestCameraPermissionsIfNeededAndPerformDenyAction(
                requestCameraActivityLauncher = requestStorageActivityLauncher!!,
                // if the user denies the permission we will make him go back to the home screen
                denyPermissionsAction = { findNavController().navigateUp() }
            )
        } else {
            setUpCameraX()
        }
    }

    fun scanImage(bitmapImage: Bitmap) {
        val image = InputImage.fromBitmap(bitmapImage, 0)
        scanner.process(image).addOnSuccessListener { barcodes ->
            Log.d(TAG, barcodes.firstOrNull()?.displayValue ?: "")
        }
            .addOnFailureListener { }
    }

    @SuppressLint("CheckResult")
    private fun setUpCameraX() {
        val obPreview = createOBPreview {
            setPreviewView(viewBinding.pvDisplay)
            setLensFacing(lensFacing)
        }
        val obImageAnalyzer = createOBImageAnalysis {
            when (scanType) {
                is DomainScanType.QRScanner -> {
                    viewBinding.ivRectView.visibility = View.GONE
                    setCropAnalyzeArea(
                        ImageCrop.WithRectangleView(
                            viewBinding.recvDecoration.also { it.visibility = View.VISIBLE }
                        )
                    )
                    setScannerType(OBScannerType.QRScannerType(object : QRScannerResultListener {
                        override fun onSuccessStringResult(result: String) {
                            viewBinding.tvResult.text = getString(R.string.result, result)
                        }
                    }))
                }

                is DomainScanType.BarcodeScanner -> {
                    viewBinding.ivRectView.visibility = View.GONE
                    setCropAnalyzeArea(
                        ImageCrop.WithRectangleView(
                            viewBinding.recvDecoration.also { it.visibility = View.VISIBLE }
                        )
                    )
                    setScannerType(OBScannerType.BarcodeScannerType(object :
                        BarcodeScannerResultListener {
                        override fun onSuccessStringResult(result: String) {
                            viewBinding.tvResult.text = getString(R.string.result, result)
                        }
                    }))
                }

                is DomainScanType.FaceScanner -> {
                    viewBinding.recvDecoration.visibility = View.GONE
                    viewBinding.ivRectView.visibility = View.VISIBLE
                    viewBinding.tvResult.text = ""
                    setScannerType(OBScannerType.FaceRecognitionScannerType(object :
                        FaceRecognitionScannerResultListener {
                        override fun onSuccessResult(
                            faces: MutableList<Face>,
                            originalBitmap: Bitmap
                        ) {
                            Log.d(
                                "generic FaceScanner:",
                                faces.joinToString { it.boundingBox.flattenToString() })
                            try {
                                viewBinding.ivRectView.post {
                                    var resultBitmap = targetBitmap
                                    for (face in faces) {
                                        resultBitmap =
                                            drawRect(resultBitmap, originalBitmap, face, lensFacing)
                                    }
                                    viewBinding.ivRectView.setImageBitmap(resultBitmap)
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                    }))
                }
            }

            setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
            setAnalyzeCallback(object : AnalyzeImageListener {
                override fun analyze(image: Bitmap) {
                    scanImage(image)
                }
            })
        }
        obImageCapture = createOBImageCapture {
            setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
            setFlashMode(ImageCapture.FLASH_MODE_OFF) // ImageCapture.FLASH_MODE_ON
            setImageCaptureCallback(this@CameraXSummaryFragment)
        }

        controller = CameraXController.getControllerForParameters(
            requireContext(),
            viewLifecycleOwner,
            obPreview,
            obImageAnalyzer,
            obImageCapture
        )
        controller?.start()
    }

    companion object {
        const val TAG = "CameraXPreviewTAG"

        /** Use external media if it is available, our app's file directory otherwise */
        fun Context.getOutputDirectory(): File {
            val appContext = applicationContext
            val mediaDir = externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

    override fun onSuccess(uri: Uri) {
        Log.d("ImageCaptureSuccess", "onSuccess: ${uri.path}")
    }

    override fun onError(e: Exception) {
        Log.d("ImageCaptureError", "onError: $e")
        e.printStackTrace()
    }
}
