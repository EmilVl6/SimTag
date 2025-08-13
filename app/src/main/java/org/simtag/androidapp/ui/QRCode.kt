package org.simtag.androidapp.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QRCode(content: String, size: Int = 128, modifier: Modifier = Modifier) {
    val bitmap = remember(content) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb())
            }
        }
        bmp.asImageBitmap()
    }
    Image(
        bitmap = bitmap,
        contentDescription = "QR Code",
        modifier = modifier.size(size.dp),
        contentScale = ContentScale.Fit
    )
}