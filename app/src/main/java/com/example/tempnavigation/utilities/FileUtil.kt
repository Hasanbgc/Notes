package com.example.tempnavigation.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.core.net.toUri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileUtil {
    companion object {
        private fun createAppDirectoryInDownloads(context: Context): File? {
            val downloadsDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDirectory = File(downloadsDirectory, "TempNavPicture")

            if (!appDirectory.exists()) {
                val directoryCreated = appDirectory.mkdir()
                if (!directoryCreated) {
                    // Failed to create the directory
                    return null
                }
            }

            return appDirectory
        }

        private fun createFileInAppDirectory(context: Context, fileName: String): File? {
            val appDirectory = createAppDirectoryInDownloads(context)
            if (appDirectory != null) {
                val file = File(appDirectory, fileName)
                try {
                    if (!file.exists()) {
                        val fileCreated = file.createNewFile()
                        if (!fileCreated) {
                            // Failed to create the file
                            return null
                        }
                    }
                    return file
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return null
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

        fun saveImage(context: Context, imageFileName: String, bitmapImage: Bitmap):String {
            val newFile = createFileInAppDirectory(context, imageFileName)
            try {
                val outputStream = FileOutputStream(newFile)
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                outputStream.close()
            }catch (e:IOException){
                e.printStackTrace()
            }
            return newFile!!.absolutePath
        }

        fun getImageFromInternalStorage(directory:String): Bitmap? {
            val file = File(directory)
            return BitmapFactory.decodeStream(FileInputStream(file))
        }

        fun deleteImageFromInternalStorage(directory: String): Boolean {
            val file = File(directory)
            return file.delete()
        }
    }
}