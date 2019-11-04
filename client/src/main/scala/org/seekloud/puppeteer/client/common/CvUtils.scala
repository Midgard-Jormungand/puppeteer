package org.seekloud.puppeteer.client.common

import java.nio.ByteBuffer

import org.bytedeco.opencv.opencv_core.Mat

object CvUtils {
  @inline
  final def getDstArray(dst: Array[Byte], size: Int): Array[Byte] = {
    if (dst == null) {
      new Array[Byte](size)
    } else {
      assert(size == dst.length)
      dst
    }
  }

  final def extractMatData(mat: Mat, dstArray: Array[Byte] = null): Array[Byte] = {
    val w = mat.cols()
    val h = mat.rows()
    val c = mat.channels()
    val size = w * h * c
    println(s"w:$w, h:$h, c:$c")
    println(s"size:::$size")

    val arr = getDstArray(dstArray, size)

    val buff = mat.createBuffer().asInstanceOf[ByteBuffer]
    buff.rewind()
    buff.get(arr)
    //    val multiDimArray = Array.ofDim[Byte](w, h, c)
    //    arr.indices.foreach { i =>
    //      val wIdx = i / (h * c)
    //      val hIdx = (i % (h * c)) / c
    //      val cIdx = (i % (h * c)) % c
    //      multiDimArray(wIdx)(hIdx)(cIdx) = arr(i)
    //    }
    //    multiDimArray
    arr
  }
}
