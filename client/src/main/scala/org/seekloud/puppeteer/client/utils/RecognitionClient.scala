package org.seekloud.puppeteer.client.utils
import org.seekloud.puppeteer.client.Boot.executor
import org.seekloud.puppeteer.shared.ptcl.Protocol.RecognizeRsp
import org.slf4j.LoggerFactory
import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.seekloud.puppeteer.client.protocol.Protocol.Vec3f

import scala.concurrent.Future

/**
  * Created by haoshuhan on 2019/5/10.
  */
object RecognitionClient extends HttpUtil {

  import io.circe.generic.auto._
  import io.circe.parser.decode
  import io.circe.syntax._

  private val log = LoggerFactory.getLogger(this.getClass)

  private val recognitionBaseUrl = "http://10.1.69.34:5000"

  def recognition(img: Array[Byte]): Future[Either[Throwable, Array[Vec3f]]] = {

    val url = recognitionBaseUrl
    postImgRequestSend("recognition", url, Nil, img).map {
      case Right(jsonStr) =>
        decode[RecognizeRsp](jsonStr).map{rsp =>
          rsp.result.map(t => Vec3f(-t._2,-t._1,-t._3))
        }
      case Left(error) =>
        log.error(s"recognition error: $error")
        Left(error)
    }
  }


}
