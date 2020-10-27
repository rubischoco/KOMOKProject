package com.teamC.komok

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class ImageUtils {
    var cameraUri: Uri? = Uri.EMPTY

    fun getImageDialog(activity: Activity, cameraCode: Int, galleryCode: Int) {
        val customDialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_choose)
            setCancelable(true)
        }
        customDialog.show()

        val buttonCamera = customDialog.findViewById<LinearLayout>(R.id.button_camera)
        val buttonGallery = customDialog.findViewById<LinearLayout>(R.id.button_gallery)

        buttonCamera.setOnClickListener {
            customDialog.dismiss()
            getImageFromCamera(activity, cameraCode)
        }

        buttonGallery.setOnClickListener {
            customDialog.dismiss()
            getImageFromGallery(activity, galleryCode)
        }
    }

    private fun getImageFromCamera(activity: Activity, getCode: Int, folderName: String="KOMOK") {
        var values = ContentValues()
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            values = pathNewer(folderName)
        } else {
            values.put(MediaStore.Images.Media.DATA, pathOlder(folderName).absolutePath)
        }
        cameraUri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //Camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        activity.startActivityForResult(cameraIntent, getCode)
    }

    fun cancelCamera(activity: Activity) {
        cameraUri?.let { activity.contentResolver.delete(it, null, null) }
    }

    private fun getImageFromGallery(activity: Activity, getCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity.startActivityForResult(intent, getCode)
    }

    // mendapatkan bitmap dari uri (ML Kit Samples)
    fun getBitmapFromContentUri(contentResolver: ContentResolver, imageUri: Uri): Bitmap {
        val decodedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val orientation = getExifOrientationTag(contentResolver, imageUri)
        var rotationDegrees = 0
        var flipX = false
        var flipY = false
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipX = true
            ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                rotationDegrees = 90
                flipX = true
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees = 180
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipY = true
            ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees = -90
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                rotationDegrees = -90
                flipX = true
            }
            ExifInterface.ORIENTATION_UNDEFINED, ExifInterface.ORIENTATION_NORMAL -> {
            }
        }
        return rotateBitmap(decodedBitmap, rotationDegrees, flipX, flipY)
    }
    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean): Bitmap {
        val matrix = Matrix()
        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())
        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) { bitmap.recycle() }
        return rotatedBitmap
    }
    private fun getExifOrientationTag(resolver: ContentResolver, imageUri: Uri): Int {
        if (ContentResolver.SCHEME_CONTENT != imageUri.scheme
            && ContentResolver.SCHEME_FILE != imageUri.scheme
        ) {
            return 0
        }
        var exif: ExifInterface
        try {
            resolver.openInputStream(imageUri).use { inputStream ->
                if (inputStream == null) {
                    return 0
                }
                exif = ExifInterface(inputStream)
            }
        } catch (e: IOException) {
            return 0
        }
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }

    // https://stackoverflow.com/questions/36624756/how-to-save-bitmap-to-android-gallery
    fun saveImage(context: Context, bitmap: Bitmap, folderName: String="KOMOK") {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = pathNewer(folderName)
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri: Uri? = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val file = pathOlder(folderName)
            saveImageToStream(bitmap, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }
    private fun contentValues() : ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
    }
    private fun pathNewer(folderName: String): ContentValues {
        // RELATIVE_PATH and IS_PENDING are introduced in API 29.
        return contentValues().apply {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
        }
    }
    private fun pathOlder(folderName: String): File {
        // getExternalStorageDirectory is deprecated in API 29
        val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + folderName)
        if (!directory.exists()) { directory.mkdirs() }
        val fileName = System.currentTimeMillis().toString() + ".png"
        return File(directory, fileName)
    }
    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // https://stackoverflow.com/questions/9049143/android-share-intent-for-a-bitmap-is-it-possible-not-to-save-it-prior-sharing?rq=1
    fun shareImage(context: Context, bitmap: Bitmap, text: String="#KOMOK") {
        // We need date and time to be added to image name to make it unique every time, otherwise bitmap will not update
        val imageName = "/image_${System.currentTimeMillis()}.png"
        // SAVE
        try {
            File(context.cacheDir, "images").deleteRecursively() // delete old images
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream = FileOutputStream("$cachePath$imageName")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) // can be png and any quality level
            stream.close()
        } catch (ex: Exception) {
            Log.e("SHARE ERROR", ex.toString())
        }
        // SHARE
        val imagePath = File(context.cacheDir, "images")
        val newFile = File(imagePath, imageName)
        val contentUri = FileProvider.getUriForFile(
            context,
            "com.teamC.komok.fileprovider",
            newFile
        )
        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.type = "image/png" // just assign type. we don't need to set data, otherwise intent will not work properly
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            context.startActivity(Intent.createChooser(shareIntent, "Choose app"))
        }
    }
}