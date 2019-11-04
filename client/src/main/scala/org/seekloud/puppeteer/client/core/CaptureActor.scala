package org.seekloud.puppeteer.client.core

import java.nio.channels.{Channels, Pipe}

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import javax.sound.sampled._
import org.bytedeco.opencv.global.opencv_videoio
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.seekloud.puppeteer.client.Boot.blockingDispatcher
import org.seekloud.puppeteer.client.common.{Constants, CvUtils}
import org.slf4j.LoggerFactory
import javafx.scene.canvas.GraphicsContext

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
  private val AUDIO_DEVICE_INDEX = 4
  val frameRate = 30
  val frameDuration: Float = 1000.0f / 30
  val audioChannels = 1
  private var frameCount = 0
  val aiMap = new mutable.HashMap[Byte, Byte].empty
  private var camWidth = 0
  private var camHeight = 0
  private var liveState = false


  //延时buffer
  /**
    * 定时器使用audioCapture中的定时
    * 其他需要定时器如编码可发送消息
    **/


  trait Command

  final case class DevicesReady(gc: GraphicsContext) extends Command

  final case class ChangeAi(changeIndex: Byte, changeValue: Byte) extends Command

  final case class StartLive(liveId: String, liveCode: String) extends Command

  final case object PushAuthFailed extends Command

  final case object StopEncode extends Command with DrawCommand with AudioCommand

  final case class ChildDead[U](name: String, childRef: ActorRef[U]) extends Command

  trait VideoCommand

  final case object ReadMat extends VideoCommand

  trait AudioCommand

  final case class Encoder(videoEncoder: ActorRef[EncodeActor.VideoEncodeCommand]) extends Command with AudioCommand

  final case class SetSampleInterval(line: TargetDataLine, audioBytes: Array[Byte]) extends AudioCommand

  final case class Sample(line: TargetDataLine, audioBytes: Array[Byte]) extends AudioCommand

  trait DrawCommand

  final case object DrawFrame extends DrawCommand

  final case object DrawKey

  final case object Count extends DrawCommand

  final case object TimerKey4Count

  final case object TimerKey4Draw

  trait DetectCommand

  final case object TimerKey4Detect

  final case object Detect extends DetectCommand

  /**
    * 控制消息
    **/
  final case object DeviceOn extends Command with VideoCommand with AudioCommand with DrawCommand with DetectCommand

  final case object DeviceOff extends Command with VideoCommand with AudioCommand with DrawCommand with DetectCommand

  def create(): Behavior[Command] = Behaviors.setup[Command] { ctx =>
    log.info("create| start..")
    idle("idle|")
  }


  private def idle(
                    logPrefix: String,
                  ): Behavior[Command] = Behaviors.receive[Command] { (ctx, msg) =>
    msg match {
      case msg: DevicesReady =>
        log.info(s"$logPrefix receive deviceOn")
        try {
          val cam = new VideoCapture(WEBCAM_DEVICE_INDEX)
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
                   encodeActor: Option[ActorRef[EncodeActor.EncodeCommand]] = None
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
          encodeActor.foreach(_ ! EncodeActor.StopEncode)
          idle("idle")

        case StopEncode =>
          liveState = false
          log.info(s"$logPrefix receive StopEncode")
          encodeActor.foreach(_ ! EncodeActor.StopEncode)
          work(logPrefix, videoActor, None)


        case msg: StartLive =>
          log.info(s"$logPrefix receive StartLive $msg")
          val pipe = Pipe.open() //ts流通道
        val encodeActor = ctx.spawn(EncodeActor.create("create|"), "encodeActor", blockingDispatcher)
          encodeActor ! EncodeActor.EncodeInit(Channels.newOutputStream(pipe.sink()), camWidth, camHeight)
          work(logPrefix, videoActor, Some(encodeActor))

        case msg: Encoder =>
          log.info(s"$logPrefix receive Encoder")
          Behaviors.same

        case PushAuthFailed =>
          log.info(s"$logPrefix receive PushAuthFailed")
          //todo:待处理
          Behaviors.same

        case unKnow =>
          log.error(s"$logPrefix receive a unknow $unKnow")
          Behaviors.same
      }
    }

  // act as new actor spawned by the parent.ctx
  private def videoCapture(logPrefix: String,
                           cam: VideoCapture,
                          ): Behavior[VideoCommand] =
    Behaviors.receive[VideoCommand] { (ctx, msg) =>
      msg match {
        case DeviceOn =>
          log.info(s"$logPrefix Media camera start.")
          ctx.self ! ReadMat
          Behaviors.same

        case ReadMat =>
          val frame = new Mat
          if (cam.read(frame)) {
            val rstArray = CvUtils.extractMatData(frame)
            RecognitionClient.recognition(rstArray).map {
              case Right(rsp) =>
                ctx.self ! rsp

              case Left(error) =>
                log.error("======error=======")
            }
          } else {
            //fixme 此处存在error
            log.error(s"$logPrefix readMat error")
            System.exit(0)
          }

          ctx.self ! ReadMat
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
