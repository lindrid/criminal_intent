package com.example.criminal_intent

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.media.ExifInterface
import android.net.Uri
import kotlin.math.roundToInt

fun getScaledBitmap (path: String, destWidth: Int, destHeight: Int): Bitmap {
  var options = BitmapFactory.Options()
  options.inJustDecodeBounds = true
  BitmapFactory.decodeFile(path, options)

  val srcWidth = options.outWidth.toFloat()
  val srcHeight = options.outHeight.toFloat()

  var inSampleSize = 1
  if (srcWidth > destWidth || srcHeight > destHeight) {
    val widthScale = srcWidth / destWidth
    val heightScale = srcHeight / destHeight

    val sampleScale = if (widthScale > heightScale) {
      widthScale
    } else {
      heightScale
    }
    inSampleSize = sampleScale.roundToInt()
  }

  options = BitmapFactory.Options()
  options.inSampleSize = inSampleSize

  return BitmapFactory.decodeFile(path, options)
}

fun getScaledBitmap (path: String, activity: Activity): Bitmap {
  val size = Point()
  activity.windowManager.defaultDisplay.getSize(size)

  return getScaledBitmap(path, size.x, size.y)
}

fun getRotation (context: Context, photoUri: Uri): Float {
  val inputStream = context.contentResolver.openInputStream(photoUri)
  val exifInterface = ExifInterface(requireNotNull(inputStream))

  var rotation = 0f

  when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
    ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90f
    ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180f
    ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270f
  }

  return rotation
}

fun createFlippedBitmap(source: Bitmap): Bitmap? {
  val matrix = Matrix()
  matrix.postScale(
    -1f, 1f,
    source.width / 2f,
    source.height / 2f
  )
  return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}