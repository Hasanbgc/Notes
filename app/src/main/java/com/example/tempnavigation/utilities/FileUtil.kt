package com.example.tempnavigation.utilities

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class FileUtil {

    companion object {
        var appFileDirectory = File("")
        val TAG = "FileUtil"
        private fun createAppDirectory(context: Context): File? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (appFileDirectory.exists() && appFileDirectory.isDirectory) {
                    return appFileDirectory
                }
                val imageCollections = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                //val dcimCollection = MediaStore.Images.Media.getContentUri(MediaStore.Vo)
                val resolver: ContentResolver = context.contentResolver

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "TempNavPicture.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + "TempNavPicture"
                    )
                }
                try {
                    val directoryUri =
                        resolver.insert(imageCollections, contentValues)
                    Log.d(TAG, "directoryUri = $directoryUri")
                    if (directoryUri != null) {
                        appFileDirectory = File(directoryUri.path)
                    }
                } catch (e: Exception) {
                    e.message
                }


                return appFileDirectory
            } else {

                val downloadsDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                val appDirectory = File(downloadsDirectory, "TempNavPicture")

                if (!appDirectory.exists()) {
                    val directoryCreated = appDirectory.mkdir()
                    if (!directoryCreated) {
                        throw NullPointerException("Directory is not created")
                    }
                }

                return appDirectory
            }
        }

        private fun createFileInAppDirectory(context: Context, fileName: String): File? {
            var file = File("")
            val appDirectory = createAppDirectory(context)
            if (appDirectory != null) {
                file = File(appDirectory, fileName)
                try {
                    if (!file.exists()) {
                        val fileCreated = file.createNewFile()
                        if (!fileCreated) {
                            throw IOException("Failed to create file")
                        }
                    }
                    return file
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return file
        }

        fun saveToInternalStorage(
            context: Context,
            bitmapImage: Bitmap,
            imageFileName: String
        ): String {
            context.openFileOutput(imageFileName, Context.MODE_PRIVATE).use { fos ->
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 25, fos)
            }
            return context.filesDir.absolutePath
        }

        fun saveImage(context: Context, imageFileName: String, bitmapImage: Bitmap): String {
            val newFile = createFileInAppDirectory(context, imageFileName)
            val resolver = context.contentResolver
            if (appFileDirectory != null) {
                return try {
                    Log.d(TAG, "app file directory = $appFileDirectory")
                    resolver.openOutputStream(appFileDirectory.toUri(), "w").use { os ->
                        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, os!!)
                    }
                    appFileDirectory.toURI().toString()
                } catch (e: FileNotFoundException) {
                    // Some legacy devices won't create directory for the Uri if dir not exist, resulting in
                    // a FileNotFoundException. To resolve this issue, we should use the File API to save the
                    // image, which allows us to create the directory ourselves
                    ""
                }
            } else {
                try {
                    if (newFile != null) {
                        val outputStream = FileOutputStream(newFile)
                        bitmapImage.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                        outputStream.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return newFile!!.absolutePath
        }


        @RequiresApi(Build.VERSION_CODES.P)
        fun getImageFromInternalStorage(context: Context, filePath: String): Bitmap? {
            val trimmedFilePath = filePath.trim()
            val file = File(trimmedFilePath)
            val name = file.name
            Log.d(TAG, "file name = $name")
            return if (file.exists()) {
                Log.d(TAG,"filepath = $filePath")
                val uri = Uri.fromFile(file)
                Log.d(TAG,"URI = $uri")
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                // The file does not exist
                null
            }
        }
        fun getimage(context: Context,filePath: String):Uri {
            var uri = Uri.EMPTY
            val fileUri = Uri.parse(filePath)
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = MediaStore.Images.Media.DATA + "=?"
            val selectionArgs = arrayOf(fileUri.path)
            val sortOrder = "ascending"

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val mediaItemId = cursor.getLong(idColumnIndex)
                    uri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaItemId.toString()
                    )
                    // Use the `mediaItemUri` representing the media item itself.
                    // You can pass it to an image-viewing component or perform other operations.
                }
            }
            return uri
        }

        fun deleteImageFromInternalStorage(directory: String): Boolean {
            val file = File(directory)
            return file.delete()
        }

        fun saveBitmapToAppFolderAndGetPath(context: Context, bitmap: Bitmap, fileName: String): String? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = context.contentResolver

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + "TempNavPicture"
                    )
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
                    outputStream?.use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    getFilePathFromUri(context, imageUri)
                } else {
                    null
                }
            } else {
                val appDirectory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "TempNavPicture"
                )
                if (!appDirectory.exists()) {
                    val directoryCreated = appDirectory.mkdirs()
                    if (!directoryCreated) {
                        throw IOException("Failed to create app folder")
                    }
                }

                val file = File(appDirectory, fileName)
                val outputStream: OutputStream? = FileOutputStream(file)
                outputStream?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                file.absolutePath
            }
        }
        fun getFilePathFromUri(context: Context, uri: Uri): String? {
            val projection = arrayOf(MediaStore.MediaColumns.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    return it.getString(columnIndex)
                }
            }
            return null
        }
    }
}