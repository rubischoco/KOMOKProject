package com.teamC.komok

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.face.Face
import java.io.FileDescriptor
import kotlin.math.ceil
import kotlin.math.floor

class DrawUtils {
    private val dot = Paint()
    private val fillBox = Paint()
    private val strokeLine = Paint()

    init {
        dot.apply {
            color = Color.GREEN
            style = Paint.Style.FILL_AND_STROKE
        }
        fillBox.apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        strokeLine.apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
    }

    // mendapatkan bitmap dari uri
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(
            uri,
            "r"
        )
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        return BitmapFactory.decodeFileDescriptor(fileDescriptor)
    }

    // mendapatkan potongan gambar wajah pada gambar
    fun cropFaces(bitmap: Bitmap, curFace: Face, size: Int = 50): Bitmap {
        // mendapatkan titik kotak untuk wajah dan merubah ukuran
        val faceRect = getRectBound(bitmap, resizeRect(curFace.boundingBox, size))
        // buat bitmap kosong dengan ukuran kotak wajah yang akan diambil
        val tempBitmap = Bitmap.createBitmap(
            faceRect.width(),
            faceRect.height(),
            Bitmap.Config.ARGB_8888
        )
        val tempCanvas = Canvas(tempBitmap)
        // mengambil bagian pada gambar
        tempCanvas.drawBitmap(
            bitmap,
            // ambil pada bitmap di titik kotak faceRect
            faceRect,
            // taruh di hasil gambar
            Rect(0, 0, faceRect.width(), faceRect.height()),
            null
        )
        return tempBitmap
    }

//    // mendapatkan bagian gambar wajah pada gambar
//    fun maskFaces(bitmap: Bitmap, curFace: Face, size: Int = 50): Bitmap {
//        // buat bitmap kosong dengan ukuran sesuai bitmap pilihan
//        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//        val tempCanvas = Canvas(tempBitmap)
//        // menggambar kotak isi disekitar area wajah
//        tempCanvas.drawRect(resizeRect(curFace.boundingBox, size), fillBox)
//        // fungsi untuk hanya mendapatkan area gambar yang terdapat pada kotak isi
//        fillBox.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//        tempCanvas.drawBitmap(bitmap, 0f, 0f, fillBox)
//        // mengembalikan metode kotak isi
//        fillBox.xfermode = null
//
//        return tempBitmap
//    }
//
//    fun swappFaces(bitmap: Bitmap, faces: MutableList<Face>): Bitmap {
//        // buat bitmap kosong dengan ukuran sesuai bitmap pilihan
//        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//        val tempCanvas = Canvas(tempBitmap)
//
//        for (face in faces) {
//            tempCanvas.drawPath(getFacePath(face.allContours[0].points), fillBox)
//        }
//
//        // fungsi untuk hanya mendapatkan area gambar yang terdapat pada kotak isi
//        fillBox.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//        tempCanvas.drawBitmap(bitmap, 0f, 0f, fillBox)
//        // mengembalikan metode kotak isi
//        fillBox.xfermode = null
//
//        return tempBitmap
//    }

    // mendapatkan gambar dengan sebagian wajah ditutupi kotak
    fun fillFaces(bitmap: Bitmap, faces: MutableList<Face>, curFace: Face): Bitmap {
        // buat bitmap kosong dengan ukuran sesuai bitmap pilihan
        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0f, 0f, null)
        // untuk setiap wajah
        for (face in faces) {
            // bukan wajah pilihan akan ditutupi kotak isi
            if (face != curFace) {
                tempCanvas.drawRect(face.boundingBox, fillBox)
            }
        }
        return tempBitmap
    }

    fun swapFaces(bitmap: Bitmap, faces: MutableList<Face>, pos: Int=0, duplicate: Boolean=false): Bitmap {
        // buat bitmap hasil
        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0f, 0f, null)
        // buat bitmap potongan wajah
        val maskBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBitmap)
        // buat simpan kotak lingkar wajah
        val tempList = mutableListOf<Rect>()

        for (face in faces) {
            // gambar jalur lingkar wajah dengan fillbox
            maskCanvas.drawPath(getFacePath(face.allContours[0].points), fillBox)
            // tambahkan kotak lingkar wajah
            tempList.add(getFaceBound(face.allContours[0].points))
        }
        // fungsi untuk hanya mendapatkan area gambar yang terdapat pada fillbox
        fillBox.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        maskCanvas.drawBitmap(bitmap, 0f, 0f, fillBox)
        fillBox.xfermode = null

        var i = pos
        for (index in 0 until tempList.size) {
            if (!duplicate) {
                i = when (i) {
                    tempList.size - 1 -> 0
                    else -> i + 1
                }
            }
            // gambar potongan wajah lain pada wajah lain
            tempCanvas.drawBitmap(
                // sumber gambar potongan wajah
                maskBitmap,
                // posisi potongan wajah
                tempList[i],
                // posisi wajah
                tempList[index],
                null
            )
        }
        return tempBitmap
    }

    // mendapatkan ukuran kotak baru
    private fun resizeRect(rect: Rect, size: Int): Rect {
        return Rect(
            rect.left - size, rect.top - size,
            rect.right + size, rect.bottom + size
        )
    }

    // mendapatkan ukuran kotak baru yang tidak di luar gambar
    private fun getRectBound(bitmap: Bitmap, rect: Rect): Rect {
        return Rect(
            if (rect.left < 0) 0 else rect.left,
            if (rect.top < 0) 0 else rect.top,
            if (rect.right > bitmap.width) bitmap.width else rect.right,
            if (rect.bottom > bitmap.height) bitmap.height else rect.bottom
        )
    }

    // mendapatkan jalur lingkar wajah untuk digambar
    private fun getFacePath(contourPoints: MutableList<PointF>): Path {
        val path = Path()
        for ((index, point) in contourPoints.withIndex()) {
            when (index) {
                0 -> path.moveTo(point.x, point.y)
                else -> path.lineTo(point.x, point.y)
            }
        }
        path.lineTo(contourPoints[0].x, contourPoints[0].y)
        return path
    }

    // mendapatkan titik-titik paling pinggir dari lingkar wajah
    private fun getCornerPoint(Points: MutableList<PointF>): FloatArray {
        val xMin: Float = Points.minBy { it.x }?.x ?: 0F
        val yMin: Float = Points.minBy { it.y }?.y ?: 0F
        val xMax: Float = Points.maxBy { it.x }?.x ?: 0F
        val yMax: Float = Points.maxBy { it.y }?.y ?: 0F
        return floatArrayOf(xMin, yMin, xMax, yMax)
    }

    // mendapatkan titik-titik untuk kotak dari lingkar wajah
    private fun getFaceBound(contourPoints: MutableList<PointF>, size: Int=0): Rect {
        val points = getCornerPoint(contourPoints)
        return Rect(floor(points[0]).toInt()-size, floor(points[1]).toInt()-size,
            ceil(points[2]).toInt()+size, ceil(points[3]).toInt()+size)
    }

    // cek jika titik berada dalam gambar
    private fun checkBoundingPoint(points: FloatArray, bound: IntArray): Boolean {
        return (points[0] >= bound[0] && points[1] >= bound[1]
                && points[2] <= bound[2] && points[3] <= bound[3])
    }
    // cek jika titik wajah berada dalam gambar
    fun checkFacePoint(bitmap: Bitmap, faces: MutableList<Face>): MutableList<Face> {
        val bound = intArrayOf(0, 0, bitmap.width, bitmap.height)
        val newFaces = mutableListOf<Face>()
        for (face in faces) {
            if (checkBoundingPoint(getCornerPoint(face.allContours[0].points), bound)) {
                newFaces.add(face)
            }
        }
        return newFaces
    }

    // menggambar kotak garis disekitar area wajah pada gambar
    fun drawRectFaces(bitmap: Bitmap, faces: MutableList<Face>, color: Int = Color.GREEN): Bitmap {
        if (color != Color.GREEN) { strokeLine.color = color }
        // buat bitmap kosong dengan ukuran sesuai bitmap pilihan
        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0f, 0f, null)

        for (face in faces) {
            // gambar kotak disekitar area wajah
            tempCanvas.drawRect(face.boundingBox, strokeLine)
            //tempCanvas.drawCircle(face.boundingBox.exactCenterX(), face.boundingBox.exactCenterY(), 3f, dot)
        }
        if (color != Color.GREEN) { strokeLine.color = Color.GREEN }
        return tempBitmap
    }

    // menggambar jalur lingkaran wajah pada gambar
    fun drawContourFaces(bitmap: Bitmap, faces: MutableList<Face>, color: Int = Color.GREEN): Bitmap {
        if (color != Color.GREEN) { strokeLine.color = color }
        // buat bitmap kosong dengan ukuran sesuai bitmap pilihan
        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0f, 0f, null)

        for (face in faces) {
            // cek data kontur wajah
            if (face.allContours.isNotEmpty()) {
                // mendapatkan jalur lingkaran wajah
                val facePaths = getFacePath(face.allContours[0].points)
                // menggambar garis lingkaran wajah
                tempCanvas.drawPath(facePaths, strokeLine)
                //tempCanvas.drawCircle(face.boundingBox.exactCenterX(), face.boundingBox.exactCenterY(), 3f, dot)
            }
            /* untuk setiap fitur wajah
            for (contour in face.allContours) {
                // untuk setiap titik pada fitur wajah
                for (point in contour.points) {
                    tempCanvas.drawCircle(point.x, point.y, 4f, dot)
                }
            }*/
        }
        if (color != Color.GREEN) { strokeLine.color = Color.GREEN }
        return tempBitmap
    }

//    private fun checkBoundingBox(rect: Rect, bound: IntArray): Boolean {
//        return (rect.left >= bound[0] && rect.top >= bound[1]
//                && rect.right <= bound[2] && rect.bottom <= bound[3]
//                && rect.width() >= bound[4] && rect.height() >= bound[4])
//    }
//    private fun checkFaceBox(bitmap: Bitmap, faces: MutableList<Face>, minSize: Int=100): MutableList<Face> {
//        val bound = intArrayOf(0, 0, bitmap.width, bitmap.height, minSize)
//        val newFaces = mutableListOf<Face>()
//
//        for (face in faces) {
//            if (checkBoundingBox(face.boundingBox, bound)) {
//                newFaces.add(face)
//            }
//            //Log.d("SwapActivity", "!![${face.boundingBox.flattenToString()}] [${face.boundingBox.height()} ${face.boundingBox.width()}]" +
//            //            " => ${checkBoundingBox(face.boundingBox, bound)}")
//        }
//        return newFaces
//    }
//
//    // mendapatkan titik-titik lingkaran wajah untuk digambar
//    // mentranslate list bawaan api ke list untuk drawLines pada canvas
//    private fun getFaceLines(contourPoints: MutableList<PointF>): MutableList<FloatArray> {
//        val tmp = mutableListOf<FloatArray>()
//        val facePoints = mutableListOf<FloatArray>()
//
//        for ((index, point) in contourPoints.withIndex()) {
//            val last = contourPoints.lastIndex
//
//            when (index) {
//                0 -> {
//                    tmp.add(floatArrayOf(point.x, point.y))
//                    tmp.add(floatArrayOf(point.x, point.y))
//                }
//                last -> {
//                    val a = tmp[0]
//                    val b = tmp[1]
//                    facePoints.add(floatArrayOf(a[0], a[1], point.x, point.y))
//                    facePoints.add(floatArrayOf(point.x, point.y, b[0], b[1]))
//                }
//                else -> {
//                    val a = tmp[0]
//                    facePoints.add(floatArrayOf(a[0], a[1], point.x, point.y))
//                    a[0] = point.x
//                    a[1] = point.y
//                    tmp[0] = a
//                }
//            }
//        }
//        return facePoints
//    }
}