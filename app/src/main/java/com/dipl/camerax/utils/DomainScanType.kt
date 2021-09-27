package com.dipl.camerax.utils

sealed class DomainScanType {
    object BarcodeScanner : DomainScanType() {
        override fun toString(): String {
            return "Barcode Scanner"
        }
    }

    object QRScanner : DomainScanType() {
        override fun toString(): String {
            return "QR Scanner"
        }
    }

    object FaceScanner : DomainScanType() {
        override fun toString(): String {
            return "Face Scanner"
        }
    }

    fun nextType(): DomainScanType = when (this) {
        is BarcodeScanner -> QRScanner
        is QRScanner -> FaceScanner
        is FaceScanner -> BarcodeScanner
    }
}
