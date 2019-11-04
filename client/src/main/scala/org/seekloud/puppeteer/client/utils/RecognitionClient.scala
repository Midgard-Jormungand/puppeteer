package org.seekloud.puppeteer.client.utils
//import io.circe.Json
//import io.circe.generic.auto._
//import io.circe.syntax._
//import io.circe.parser._
import scala.util.parsing.json.JSON
import org.seekloud.puppeteer.client.Boot.executor
import org.seekloud.puppeteer.shared.ptcl.Protocol.Point

/**
  * Created by haoshuhan on 2019/5/10.
  */
object RecognitionClient extends HttpUtil {
  private val recognitionBaseUrl = ""



  def recognition(img: Array[Byte]) = {

    val url = recognitionBaseUrl
    postImgRequestSend("recognition", url, Nil, img).map {
      case Right(jsonStr) =>
        Right(List(Point(500,500,500),Point(550,550,550)))
      case Left(error) =>
        Left(-2)
        }
    }


}
