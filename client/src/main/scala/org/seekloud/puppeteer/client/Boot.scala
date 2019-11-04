package org.seekloud.puppeteer.client

import akka.actor.typed.{ActorRef, DispatcherSelector}
import akka.actor.{ActorSystem, Scheduler}
import akka.dispatch.MessageDispatcher
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.jme3.math.{ColorRGBA, FastMath, Quaternion, Vector3f}
import com.jme3.scene.{Node, Spatial}
import com.jme3.scene.debug.SkeletonDebugger
import org.seekloud.puppeteer.client.model.{RenderEngine, RenderModel}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * User: Arrow
  * Date: 2019/7/16
  * Time: 11:28
  */
object Boot {

  import org.seekloud.puppeteer.client.common.AppSettings._

  implicit val system: ActorSystem = ActorSystem("puppeteer", config)
  implicit val executor: MessageDispatcher = system.dispatchers.lookup("akka.actor.my-blocking-dispatcher")
  val blockingDispatcher: DispatcherSelector = DispatcherSelector.fromConfig("akka.actor.my-blocking-dispatcher")

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = Timeout(20 seconds)



  var model: RenderModel = null
  def main(args: Array[String]): Unit = {
    RenderEngine.start()
    model = RenderModel(1)
    RenderEngine.enqueueToEngine({
      model.rightUpperArmChange(0,0,FastMath.PI/2)
      model.rightForearmChange(0,0,FastMath.PI/4)
    })
  }

}

