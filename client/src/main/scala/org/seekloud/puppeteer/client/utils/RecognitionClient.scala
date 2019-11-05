package org.seekloud.puppeteer.client.utils
import org.seekloud.puppeteer.client.Boot.executor
import org.seekloud.puppeteer.shared.ptcl.Protocol.Point

import scala.concurrent.Future

/**
  * Created by haoshuhan on 2019/5/10.
  */
object RecognitionClient extends HttpUtil {
  private val recognitionBaseUrl = ""

  def recognition(img: Array[Byte]): Future[Either[Int, List[Point]]] = {
    Future(Right(List(
      Point(0,0,0),
      Point(1,1,1),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550),
      Point(550,550,550)
    )))

//    val url = recognitionBaseUrl
//    postImgRequestSend("recognition", url, Nil, img).map {
//      case Right(jsonStr) =>
//        Right(List(Point(500,500,500),Point(550,550,550)))
//      case Left(error) =>
//        Left(-2)
//    }

  }


}
