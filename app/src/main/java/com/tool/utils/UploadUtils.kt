package com.nextclass.ai.folder.utils.utils

import android.os.Environment
import android.util.Log
import com.tool.utils.MD5
import java.io.*

/**
 *    Author : wxz
 *    Time   : 2020/11/04
 *    Desc   :
 */
object UploadUtils {

    var TAG = "UploadUtils"
    var PART_SIZE = 1024 * 1024 * 4

    /**
     * 获取某一片对应的二进制数据
     * PART_SIZE：每一片的大小
     *
     * @param partSize 当前第几片
     * @param file 文件对象
     *
     * @return 对应片段的二进制数据流
     */
    fun getPartData(partSize: Int, file: File): ByteArray? {
        val result = ByteArray(PART_SIZE)
        var accessFile: RandomAccessFile? = null
        try {
            accessFile = RandomAccessFile(file, "r")
            accessFile.seek((partSize - 1L) * PART_SIZE)
            return when (val readSize = accessFile.read(result)) {
                -1 -> null
                PART_SIZE -> result
                else -> {
                    val tempArray = ByteArray(readSize)
                    System.arraycopy(result, 0, tempArray, 0, readSize)
                    tempArray
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            accessFile?.close()
        }
        return null
    }

    fun getPartData(file: File): String {
        var numsize =
            if (file.length() % PART_SIZE == 0L) file.length() / PART_SIZE else file.length() / PART_SIZE + 1
        var json = arrayOfNulls<String>(numsize.toInt())
        val result = ByteArray(PART_SIZE)
        var accessFile: RandomAccessFile? = null
        var readSize = 0
        try {
            accessFile = RandomAccessFile(file, "r")
            var num = 0
            while (accessFile.read(result).also { readSize = it } != -1) {
                json[num++] = "\"${MD5.createMd5(result)}\""
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            accessFile?.close()
        }
        return json.contentToString()
    }

    fun getPartDataArray(file: File): Array<String?> {
        var numsize =
            if (file.length() % PART_SIZE == 0L) file.length() / PART_SIZE else file.length() / PART_SIZE + 1
        var json = arrayOfNulls<String>(numsize.toInt())
        val result = ByteArray(PART_SIZE)
        var accessFile: RandomAccessFile? = null
        var readSize = 0
        try {
            accessFile = RandomAccessFile(file, "r")
            var num = 0
            while (accessFile.read(result).also { readSize = it } != -1) {
                json[num++] = "\"${MD5.createMd5(result)}\""
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            accessFile?.close()
        }
        return json
    }

    @Throws(IOException::class)
    fun splitFile(path: String?): MutableList<File> {
        if (!File(
                Environment.getExternalStorageDirectory().absolutePath
                    .toString() + "/.UploadTmp"
            ).exists()
        ) {
            File(
                Environment.getExternalStorageDirectory().absolutePath
                    .toString() + "/.UploadTmp"
            ).mkdirs()
        }
        var list: MutableList<File> = ArrayList()
        val `is`: InputStream = FileInputStream(path)
        var len = 0
        val buff = ByteArray(PART_SIZE)
        var i = 1
        while (`is`.read(buff).also { len = it } != -1) {
            val filePath: String = Environment.getExternalStorageDirectory().getAbsolutePath()
                .toString() + "/.UploadTmp/file" + i + ".tmp"
            val raf = RandomAccessFile(filePath, "rw")
            raf.write(buff, 0, len)
            raf.close()
            list.add(File(filePath))
            i++
        }
        `is`.close()
        return list
    }

    fun getPartData(files: List<File>): String {
        var json: MutableList<String> = ArrayList()
        for (file in files) {
            json.add("\"${MD5.createMd5(file)}\"")

        }
        return (json.toTypedArray() as Array<String>).contentToString()
    }

    fun getPartDatass(file: File): String {
        return arrayOf("\"${MD5.createMd5(file)}\"").contentToString()
    }
}