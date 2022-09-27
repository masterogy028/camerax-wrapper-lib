package com.dipl.camerax.utils

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Extension function with a purpose to register for the activity result when requesting [Manifest.permission.CAMERA] permissions.
 * MUST BE CALLED IN FRAGMENT'S onCreate METHOD!
 * @param [whenGranted] Lambda to be invoked in case user grants the permission
 *
 * @return [ActivityResultLauncher<String>] if the app registered the [ActivityResultLauncher]
 * @return @null if the app already has [Manifest.permission.CAMERA] permissions
 * */
fun Fragment.registerActivityResultForCameraPermission(
    whenGranted: () -> Unit = {}
): ActivityResultLauncher<String>? {
    return if (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) != PermissionChecker.PERMISSION_GRANTED
    )
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                whenGranted.invoke()
            }
        }
    else
        null
}

/**
 * Extension function that launches [requestCameraActivityLauncher] requesting the permissions once again if the user allows it.
 * Should be called in View.onClick() method with [requestCameraActivityLauncher] instance returned from [Fragment.registerActivityResultForCameraPermission]
 *
 * @param [requestCameraActivityLauncher] ActivityResultLauncher returned from [Fragment.registerActivityResultForCameraPermission]
 *
 * */
fun Fragment.requestCameraPermissionsIfNeededAndPerformDenyAction(
    requestCameraActivityLauncher: ActivityResultLauncher<String>,
    denyPermissionsAction: () -> Unit = {}
) {
    when {
        shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Allow Camera")
                .setMessage("Allow Camera Info")
                .setPositiveButton("Allow") { _, _ ->
                    requestCameraActivityLauncher.launch(Manifest.permission.CAMERA)
                }
                .setNegativeButton("Deny") { _, _ ->
                    denyPermissionsAction.invoke()
                }
                .show()
        }
        else -> {
            requestCameraActivityLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
