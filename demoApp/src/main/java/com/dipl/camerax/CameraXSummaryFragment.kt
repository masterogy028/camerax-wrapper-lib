package com.dipl.camerax

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.core.view.drawToBitmap
import androidx.navigation.fragment.findNavController
import com.dipl.camerax.databinding.FragmentCameraXSummaryBinding
import com.dipl.camerax.utils.*
import com.dipl.camerax.utils.callbacks.ImageCaptureCallbackImpl
import com.dipl.camerax.utils.callbacks.ImageSavedCallbackImpl
import com.dipl.camerax.utils.scanning_components.AnalyzeComponent
import com.dipl.cameraxlib.dsl_builders.cameraXController
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeImageListener
import com.dipl.cameraxlib.usecase.image_analysis.ImageAnalysisUseCaseParameters
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.BarcodeScannerResultListener
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.FaceRecognitionScannerResultListener
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.QRScannerResultListener
import com.dipl.cameraxlib.usecase.image_analysis.models.ImageCrop
import com.dipl.cameraxlib.usecase.image_analysis.models.OBScannerType
import com.dipl.cameraxlib.usecase.image_capture.OBImageCapture
import com.dipl.cameraxlib.usecase.image_capture.SaveImageParams
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CameraXSummaryFragment :
    ViewBindingFragment<FragmentCameraXSummaryBinding>(bindViewBy = {
        FragmentCameraXSummaryBinding.inflate(it)
    }) {

    // required for analyzing raw input from analyze use case if we do not want to use the library pre-installed ones
    @Inject
    lateinit var analyzeComponent: AnalyzeComponent

    // util for requesting camera permissions
    var requestCameraActivityLauncher: ActivityResultLauncher<String>? = null

    // variable for storing image capture use case so we can perform image capture
    private lateinit var obImageCapture: OBImageCapture

    // storing the value of camera lens facing
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT

    // scan type so we can switch scan types programmatically within this fragment
    private var scanType: DomainScanType = DomainScanType.BarcodeScanner

    // the bitmap of the view that we want to draw on
    private val targetBitmap by lazy { viewBinding.ivRectView.drawToBitmap() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // registering this fragment for onActivityResult when requesting permissions
        requestCameraActivityLauncher = registerActivityResultForCameraPermission {
            setUpCameraX()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermissions()
        viewBinding.tvScannerMode.text =
            getString(R.string.current_mode, scanType.toString())
        viewBinding.btnTakePicture.setOnClickListener {
            obImageCapture.captureAndSaveImage(
                SaveImageParams(
                    "cameraX_dipl${System.currentTimeMillis()}",
                    ".jpg",
                    requireContext().getOutputDirectory()
                )
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
        if (requestCameraActivityLauncher != null) {
            requestCameraPermissionsIfNeededAndPerformDenyAction(
                requestCameraActivityLauncher = requestCameraActivityLauncher!!,
                // if the user denies the permission we will make him go back to the home screen
                denyPermissionsAction = { findNavController().navigateUp() }
            )
        } else {
            setUpCameraX()
        }
    }

    @SuppressLint("CheckResult")
    private fun setUpCameraX() {
        cameraXController(requireContext(), viewLifecycleOwner) {
            preview {
                setPreviewView(viewBinding.pvDisplay)
                setLensFacing(lensFacing)
            }
            obImageCapture = imageCapture {
                setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY) // ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                setFlashMode(FLASH_MODE_OFF) // ImageCapture.FLASH_MODE_ON
                setImageSavedCallback(ImageSavedCallbackImpl)
                setImageCapturedCallback(ImageCaptureCallbackImpl)
            }
            imageAnalysis {
                pickScanType()
                setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                setAnalyzeCallback(object : AnalyzeImageListener {
                    override fun analyze(image: Bitmap) {
                        analyzeComponent.scanForQRCode(image).addOnSuccessListener { barcodes ->
                            Log.d(TAG, barcodes.firstOrNull()?.displayValue ?: "")
                        }
                            .addOnFailureListener { }
                    }
                })
            }
        }.start()
    }

    private fun ImageAnalysisUseCaseParameters.Builder.pickScanType() {
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
                        viewBinding.tvResult.visibility = View.VISIBLE
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
                            faces.joinToString(separator = ", ") { it.boundingBox.flattenToString() })
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
    }

    companion object {
        const val TAG = "CameraXPreviewTAG"
    }
}
