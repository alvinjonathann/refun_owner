package com.refun.owner.utils

import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.refun.owner.model.ScannedBottle
import java.time.Instant

class QrCodeUtils {
    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()

        fun generateQrCodeBitmap(scannedBottles: List<ScannedBottle>, width: Int, height: Int): Bitmap {
            val jsonData = createJsonData(scannedBottles)
            val bitMatrix = generateBitMatrix(jsonData, width, height)
            return createBitmap(bitMatrix)
        }

        private fun createJsonData(scannedBottles: List<ScannedBottle>): String {
            val bottleData = mapOf(
                "bottles" to scannedBottles.map { scannedBottle ->
                    mapOf(
                        "brand" to scannedBottle.bottle.brand,
                        "volume_ml" to scannedBottle.bottle.volumeMl,
                        "points" to scannedBottle.bottle.points,
                        "timestamp" to scannedBottle.timestamp
                    )
                }
            )
            return gson.toJson(bottleData)
        }

        private fun generateBitMatrix(data: String, width: Int, height: Int): BitMatrix {
            return MultiFormatWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                width,
                height
            )
        }

        private fun createBitmap(bitMatrix: BitMatrix): Bitmap {
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }

            return bitmap
        }
    }
} 