package org.seekloud.puppeteer.client.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import org.bytedeco.opencv.global.opencv_videoio
import org.bytedeco.opencv.opencv_core.{Mat, Size}
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.seekloud.puppeteer.client.Boot.{blockingDispatcher, executor, model}
import org.seekloud.puppeteer.client.common.{Constants, CvUtils}
import org.slf4j.LoggerFactory
import org.seekloud.puppeteer.client.model.RenderEngine
import org.seekloud.puppeteer.client.utils.MathUtil.Vec3f
import org.seekloud.puppeteer.client.utils.RecognitionClient

import concurrent.duration._

/**
  * Created by dql
  * Date on 2019/11/1
  * videoCapture/audioCapture
  */
object CaptureActor {
  private val log = LoggerFactory.getLogger(this.getClass)
  private val WEBCAM_DEVICE_INDEX = 0
  val frameRate = 30
  val frameDuration: Float = 1000.0f / 30
  private var frameCount = 0
  private var camWidth = 0
  private var camHeight = 0


  trait Command

  final case object DevicesReady extends Command

  final case class ChildDead[U](name: String, childRef: ActorRef[U]) extends Command

  trait VideoCommand

  final case object ReadMat extends VideoCommand

  final case object TimerKey4Read

  /**
    * 控制消息
    **/
  final case object DeviceOn extends Command with VideoCommand

  final case object DeviceOff extends Command with VideoCommand

  def create(): Behavior[Command] = Behaviors.setup[Command] { ctx =>
    log.info("create| start..")
    ctx.self ! DevicesReady
    idle("idle|")
  }


  private def idle(
                    logPrefix: String,
                  ): Behavior[Command] = Behaviors.receive[Command] { (ctx, msg) =>
    msg match {
      case DevicesReady =>
        log.info(s"$logPrefix receive deviceOn")
        try {
          val cam = new VideoCapture("./model/dan.mp4")
          cam.set(opencv_videoio.CAP_PROP_FRAME_WIDTH, Constants.DefaultPlayer.width)
          cam.set(opencv_videoio.CAP_PROP_FRAME_HEIGHT, Constants.DefaultPlayer.height)
          camWidth = cam.get(opencv_videoio.CAP_PROP_FRAME_WIDTH).toInt
          camHeight = cam.get(opencv_videoio.CAP_PROP_FRAME_HEIGHT).toInt
          log.info(s"$logPrefix device width=${cam.get(opencv_videoio.CAP_PROP_FRAME_WIDTH)} height=${cam.get(opencv_videoio.CAP_PROP_FRAME_HEIGHT)} fps=${cam.get(opencv_videoio.CAP_PROP_FPS)}")

          val videoActor = ctx.spawn(videoCapture("videoCapture|", cam), "videoActor", blockingDispatcher)

          ctx.self ! DeviceOn
          work("work|", videoActor)
        } catch {
          case ex: Exception =>
            log.debug(s"camera grabber start error: $ex")
            Behaviors.same
        }

      case unKnow =>
        log.error(s"$logPrefix receive a unknow $unKnow")
        Behaviors.same
    }
  }

  // act as new state
  private def work(logPrefix: String,
                   videoActor: ActorRef[VideoCommand],
                  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case DeviceOn =>
          log.info(s"$logPrefix start media devices.")
          videoActor ! DeviceOn
          Behaviors.same

        case DeviceOff =>
          log.info(s"$logPrefix stop media devices.")
          videoActor ! DeviceOff
          idle("idle")

        case unKnow =>
          log.error(s"$logPrefix receive a unknow $unKnow")
          Behaviors.same
      }
    }


  private var recognizing = false
  // act as new actor spawned by the parent.ctx
  private def videoCapture(logPrefix: String,
                           cam: VideoCapture,
                          ): Behavior[VideoCommand] =
    Behaviors.withTimers[VideoCommand] { timer =>
      Behaviors.receive[VideoCommand] { (ctx, msg) =>
        msg match {
          case DeviceOn =>
            log.info(s"$logPrefix Media camera start.")

//            ctx.self ! ReadMat
            timer.startPeriodicTimer(TimerKey4Read, ReadMat, 2.seconds)
            Behaviors.same

          case ReadMat =>
            val t0 = System.currentTimeMillis()
            println("get readMat")
            if (!recognizing) {
              recognizing = true
              println("recognize")
              val frame = new Mat
              if (cam.read(frame)) {
                val t1 = System.currentTimeMillis()
                val dstImg = new Mat()
                CvUtils.resize(frame, dstImg, 368, 368)
                val t2 = System.currentTimeMillis()
                val rstArray = CvUtils.extractMatData(dstImg)
                val t3 = System.currentTimeMillis()
                RecognitionClient.recognition(rstArray).map {
                  case Right(rsp) =>
                    controlModel(rsp)
                    recognizing = false
                    val t4 = System.currentTimeMillis()
                    println("总时长 ", t4 - t0, "    cam read时长 ", t1 - t0, "    resize时长 ", t2 - t1, "    extractMatData时长 ", t3 - t2, "    识别时长 ", t4 - t3)
                  case Left(error) =>
                    log.error("======error=======")
                    recognizing = false
                }
              } else {
                  //fixme 此处存在error
                  log.error(s"$logPrefix readMat error")
                  System.exit(0)
              }
            }
            //
            //          ctx.self ! ReadMat
            Behaviors.same

          case DeviceOff =>
            log.info(s"$logPrefix Media camera stopped.")
            cam.release()
            Behaviors.stopped

          case unKnow =>
            log.error(s"$logPrefix receive a unknow $unKnow")
            Behaviors.same
        }
      }
    }


  def controlModel(pointArray:Array[Vec3f]): Unit = {
    // 3d point
//    println(pointArray.zipWithIndex.mkString("\n"))
    val hip = pointArray.head
    val hipR = pointArray(1)
    val kneeR = pointArray(2)
    val footR = pointArray(3)
    val hipL = pointArray(4)
    val kneeL = pointArray(5)
    val footL = pointArray(6)
    val spine = pointArray(7)
    val thorax = pointArray(8)
    val neck = pointArray(9)
    val head = pointArray(10)
    val shoulderL = pointArray(11)
    val elbowL = pointArray(12)
    val wristL = pointArray(13)
    val shoulderR = pointArray(14)
    val elbowR = pointArray(15)
    val wristR = pointArray(16)

    // 3d point to bone vector
    val headVecAtOrigin = head - neck
    val headVec = Vec3f(headVecAtOrigin.x, -headVecAtOrigin.y, -headVecAtOrigin.z)

    val neckVecAtOrigin = neck - thorax
    val neckVec = Vec3f(neckVecAtOrigin.x, -neckVecAtOrigin.y, -neckVecAtOrigin.z)

    val upperArmLVecAtOrigin = elbowL - shoulderL
    val upperArmLVec = Vec3f(upperArmLVecAtOrigin.y, upperArmLVecAtOrigin.x, -upperArmLVecAtOrigin.z)

    val forearmLVecAtOrigin = wristL - elbowL
    val forearmLVec = Vec3f(forearmLVecAtOrigin.y, forearmLVecAtOrigin.x, -forearmLVecAtOrigin.z)

    val upperArmRVecAtOrigin = elbowR - shoulderR
    val upperArmRVec = Vec3f(-upperArmRVecAtOrigin.y, -upperArmRVecAtOrigin.x, -upperArmRVecAtOrigin.z)

    val forearmRVecAtOrigin = wristR - elbowR
    val forearmRVec = Vec3f(-forearmRVecAtOrigin.y, -forearmRVecAtOrigin.x, -forearmRVecAtOrigin.z)

    // change model
    if (model != null) {
      RenderEngine.enqueueToEngine({
        model.upperArmRightChange(upperArmRVec)
        model.forearmRightChange(forearmRVec, upperArmRVec)
        model.upperArmLeftChange(upperArmLVec)
        model.forearmLeftChange(forearmLVec, upperArmLVec)
        model.neckChange(neckVec)
        model.headChange(headVec, neckVec)
      })
    }
  }
}


