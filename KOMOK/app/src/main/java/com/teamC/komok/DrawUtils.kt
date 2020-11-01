package com.teamC.komok

import android.graphics.*
import com.google.mlkit.vision.face.Face
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
            strokeWidth = 5f
        }
    }

    // mendapatkan potongan gambar wajah pada gambar
    fun cropShapeFaces(
        bitmap: Bitmap,
        curFace: Face,
        shape: Boolean = true,
        size: Int = 0
    ): Bitmap {
        // mendapatkan titik kotak untuk wajah dan merubah ukuran
//        val faceRect = resizeRect(curFace.boundingBox, size)
        val faceRect = getRectBound(bitmap, resizeRect(curFace.boundingBox, size))
        // buat bitmap kosong dengan ukuran kotak wajah yang akan diambil
        val tempBitmap = Bitmap.createBitmap(
            faceRect.width(),
            faceRect.height(),
            Bitmap.Config.ARGB_8888
        )
        val tempCanvas = Canvas(tempBitmap)
        if (!shape) {
            tempCanvas.drawOval(0f, 0f, faceRect.width() + 0f, faceRect.height() + 0f, fillBox)
            //tempCanvas.drawCircle(faceRect.width()/2f, faceRect.height()/2f, faceRect.width()/2f, fillBox)
            fillBox.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
        // mengambil bagian pada gambar
        tempCanvas.drawBitmap(
            bitmap,
            // ambil pada bitmap di titik kotak faceRect
            faceRect,
            // taruh di hasil gambar
            Rect(0, 0, faceRect.width(), faceRect.height()),
            if (!shape) {
                fillBox
            } else {
                null
            }
        )
        if (!shape) {
            fillBox.xfermode = null
        }
        return tempBitmap
    }

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

    fun swapFaces(
        bitmap: Bitmap,
        faces: MutableList<Face>,
        pos: Int = 0,
        duplicate: Boolean = false
    ): Bitmap {
        // buat bitmap hasil
        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0f, 0f, null)
        // buat bitmap potongan wajah
        val maskBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBitmap)
        // buat bitmap potongan wajah (flipped)
        val flipBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val flipCanvas = Canvas(flipBitmap)
        // buat simpan kotak lingkar wajah
        val tempList = mutableListOf<Rect>()
        val flipList = mutableListOf<Rect>()
        val angleList = mutableListOf<Float>()
        // buat matrix untuk flip path
        val matrix = Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }

        for (face in faces) {
            // gambar jalur lingkar wajah dengan fillbox
            val path = getFacePath(face.allContours[0].points)
            maskCanvas.drawPath(path, fillBox)
            // gambar jalur lingkar wajah berlawanan
            path.transform(matrix)
            flipCanvas.drawPath(path, fillBox)
            // dapatkan kotak lingkar wajah
            val bound = getFaceBound(face.allContours[0].points)
            tempList.add(bound)
            flipList.add(getFlipped(bound, bitmap.width))
            angleList.add(face.headEulerAngleY)
        }
        // flip canvas
        flipCanvas.scale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        // fungsi untuk hanya mendapatkan area gambar yang terdapat pada fillbox
        fillBox.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        maskCanvas.drawBitmap(bitmap, 0f, 0f, fillBox)
        flipCanvas.drawBitmap(bitmap, 0f, 0f, fillBox)
        fillBox.xfermode = null

        var i = pos
        for (index in 0 until tempList.size) {
            // jika bukan mode duplicate, i akan bertambah sehingga wajah tertukar secara merata
            if (!duplicate) {
                i = when (i) {
                    tempList.size - 1 -> 0
                    else -> i + 1
                }
            }
            // cek arah wajah asli dan arah wajah yang akan ditukar
            if ((angleList[index] >= 0) == (angleList[i] >= 0)) {
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
            } else {
                // gambar potongan wajah lain pada wajah lain
                tempCanvas.drawBitmap(
                    // sumber gambar potongan wajah (flipped)
                    flipBitmap,
                    // posisi potongan wajah (flipped)
                    flipList[i],
                    // posisi wajah
                    tempList[index],
                    null
                )
            }
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

    // mendapatkan posisi kotak baru yang di flip
    private fun getFlipped(rect: Rect, bitWidth: Int): Rect {
        val xLeft = bitWidth - (rect.left + rect.width())
        return Rect(
            xLeft,
            rect.top,
            xLeft + rect.width(),
            rect.bottom
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
        val xMin: Float = Points.minByOrNull { it.x }?.x ?: 0F
        val yMin: Float = Points.minByOrNull { it.y }?.y ?: 0F
        val xMax: Float = Points.maxByOrNull { it.x }?.x ?: 0F
        val yMax: Float = Points.maxByOrNull { it.y }?.y ?: 0F
        return floatArrayOf(xMin, yMin, xMax, yMax)
    }

    // mendapatkan titik-titik untuk kotak dari lingkar wajah
    private fun getFaceBound(contourPoints: MutableList<PointF>, size: Int = 0): Rect {
        val points = getCornerPoint(contourPoints)
        return Rect(
            floor(points[0]).toInt() - size, floor(points[1]).toInt() - size,
            ceil(points[2]).toInt() + size, ceil(points[3]).toInt() + size
        )
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

    // menggambar kotak / lingkaran garis disekitar area wajah pada gambar
    fun drawShapeFaces(
        bitmap: Bitmap,
        faces: MutableList<Face>,
        shape: Boolean = true,
        size: Int = 0,
        color: Int = Color.GREEN
    ): Bitmap {
        if (color != Color.GREEN) {
            strokeLine.color = color
        }
        // buat bitmap kosong dengan ukuran sesuai bitmap pilihan
        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0f, 0f, null)

        for (face in faces) {
            val faceRect = getRectBound(bitmap, resizeRect(face.boundingBox, size))
            if (shape) {
                // gambar kotak disekitar area wajah
                tempCanvas.drawRect(faceRect, strokeLine)
            } else {
                // gambar lingkarang disekitar area wajah
                tempCanvas.drawOval(RectF(faceRect), strokeLine)
                //tempCanvas.drawCircle(n.exactCenterX(), n.exactCenterY(), n.width()/2f, strokeLine)
            }
        }
        if (color != Color.GREEN) {
            strokeLine.color = Color.GREEN
        }
        return tempBitmap
    }

    // menggambar jalur lingkaran wajah pada gambar
    fun drawContourFaces(
        bitmap: Bitmap,
        faces: MutableList<Face>,
        color: Int = Color.GREEN
    ): Bitmap {
        if (color != Color.GREEN) {
            strokeLine.color = color
        }
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
        if (color != Color.GREEN) {
            strokeLine.color = Color.GREEN
        }
        return tempBitmap
    }

    fun drawWatermark(bitmap: Bitmap, watermark: Bitmap): Bitmap {
        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0f, 0f, null)

        val ratio = 1 / (watermark.width / (bitmap.width * 0.2f))
        val space = (bitmap.width / 100f) * 2
        val right = bitmap.width - (space)
        val bottom = bitmap.height - (space)
        val left = right - (watermark.width * ratio)
        val top = bottom - (watermark.height * ratio)
        tempCanvas.drawBitmap(
            watermark,
            null,
            RectF(left, top, right, bottom),
            Paint().apply { alpha = 155 }
        )
        return tempBitmap
    }
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