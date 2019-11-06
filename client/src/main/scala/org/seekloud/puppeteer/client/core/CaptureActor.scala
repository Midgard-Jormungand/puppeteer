package org.seekloud.puppeteer.client.core

import java.nio.channels.{Channels, Pipe}

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.jme3.math.{FastMath, Vector3f}
import javax.sound.sampled._
import org.bytedeco.opencv.global.opencv_videoio
import org.bytedeco.opencv.opencv_core.{Mat, Size}
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.seekloud.puppeteer.client.Boot.{blockingDispatcher, executor, model}
import org.seekloud.puppeteer.client.common.{Constants, CvUtils}
import org.slf4j.LoggerFactory
import javafx.scene.canvas.GraphicsContext
import org.seekloud.puppeteer.client.model.RenderEngine
import org.seekloud.puppeteer.client.protocol.Protocol.Vec3f
import org.seekloud.puppeteer.client.utils.RecognitionClient

import concurrent.duration._
import scala.collection.mutable

/**
  * Created by sky
  * Date on 2019/8/20
  * Time at 上午10:02
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

  trait DetectCommand

  final case object TimerKey4Read

  final case object Detect extends DetectCommand

  /**
    * 控制消息
    **/
  final case object DeviceOn extends Command with VideoCommand with DetectCommand

  final case object DeviceOff extends Command with VideoCommand with DetectCommand

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
            val frame = new Mat
            if (cam.read(frame)) {
              if (!recognizing) {
                println("recognize")
                val t1 = System.currentTimeMillis()
                recognizing = true
                val dstImg = new Mat()
                CvUtils.resize(frame, dstImg, 368, 368)
                val rstArray = CvUtils.extractMatData(dstImg)
                RecognitionClient.recognition(rstArray).map {
                  case Right(rsp) =>
                    println("关键点数量 ",rsp.length)
                    controlModel(rsp)
                    recognizing = false
                    val t3 = System.currentTimeMillis()
                    println("总时长 ",t3 - t0,"         识别时长 ",t3 - t1)
                  case Left(error) =>
                    log.error("======error=======")
                    recognizing = false
                }
              }

            } else {
              //fixme 此处存在error
              log.error(s"$logPrefix readMat error")
              System.exit(0)
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
  def controlModel(rsp:Array[Vec3f]): Unit = {
    // 3d point
    val hip = rsp.head
    val hipR = rsp(1)
    val kneeR = rsp(2)
    val footR = rsp(3)
    val hipL = rsp(4)
    val kneeL = rsp(5)
    val footL = rsp(6)
    val spine = rsp(7)
    val thorax = rsp(8)
    val neck = rsp(9)
    val head = rsp(10)
    val shoulderL = rsp(11)
    val elbowL = rsp(12)
    val wristL = rsp(13)
    val shoulderR = rsp(14)
    val elbowR = rsp(15)
    val wristR = rsp(16)
    // 3d point to bone vector
    val upperArmLVec = elbowL - shoulderL
    val forearmLVec = wristL - elbowL
    val upperArmRVec = elbowR - shoulderR
    val forearmRVec = wristR - elbowR
    // change model
    if (model != null) {
      RenderEngine.enqueueToEngine({
        model.upperArmRightChange(upperArmRVec)
        model.forearmRightChange(forearmRVec, upperArmRVec)
        model.upperArmLeftChange(upperArmLVec)
        model.forearmLeftChange(forearmLVec, upperArmLVec)
      })
    }
  }
}


