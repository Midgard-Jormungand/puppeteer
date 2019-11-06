package org.seekloud.puppeteer.shared.ptcl

/**
  * Created by haoshuhan on 2019/5/14.
  */
object Protocol {
  case class RecognizeRsp(
                         errCode: Int = 0,
                         msg: String = "ok",
                         result: Array[(Float,Float,Float)]
                         ) extends CommonRsp

}
