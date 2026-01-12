package com.darjeelingteagarden.util

import android.graphics.Bitmap
import com.squareup.picasso.Transformation

class ResizeTransformation(private val maxSize: Int) : Transformation {
    override fun transform(source: Bitmap?): Bitmap? {
        if (source == null) return null

        val width = source.width
        val height = source.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (bitmapRatio > 1) {
            newWidth = maxSize
            newHeight = (newWidth / bitmapRatio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (newHeight * bitmapRatio).toInt()
        }

        if (newWidth == width && newHeight == height) {
            // No resizing needed, return the original Bitmap
            return source
        }

        // Create a new scaled Bitmap
        val result = Bitmap.createScaledBitmap(source, newWidth, newHeight, true)

        // Safe to recycle the source as it's not being used anymore
        source.recycle()
        return result
    }

    override fun key() = "resize()"
}


//class ResizeTransformation(private val maxSize: Int) : Transformation {
//    override fun transform(source: Bitmap?): Bitmap? {
//        var result:Bitmap? = null
//
//        if (source != null) {
//            var width = source.width
//            var height = source.height
//
//            val bitmapRatio = width.toFloat() / height.toFloat()
//
//            if (bitmapRatio > 1) {
//                width = maxSize;
//                height = (width / bitmapRatio).toInt()
//            } else {
//                height = maxSize;
//                width = (height * bitmapRatio).toInt()
//            }
//
//            result = Bitmap.createScaledBitmap(source, width, height, true)
//            source.recycle()
//
//        }
//
//        return result
//    }
//
//
//    override fun key() = "resize()"
//}