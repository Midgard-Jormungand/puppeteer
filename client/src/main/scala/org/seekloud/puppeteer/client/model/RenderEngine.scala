package org.seekloud.puppeteer.client.model

import com.jme3.app.SimpleApplication
import com.jme3.asset.plugins.FileLocator
import com.jme3.input.KeyInput
import com.jme3.input.controls.{ActionListener, KeyTrigger}
import com.jme3.light.{AmbientLight, DirectionalLight}
import com.jme3.math.{ColorRGBA, Quaternion, Vector3f}

/**
  * Created by sky
  * Date on 2019/9/25
  * Time at 下午5:03
  * render2memory
  */
object RenderEngine extends SimpleApplication {
  private val KeyR = "keyR"

  private val KeyT = "keyT"

  def gamePause(): Unit = enqueueToEngine({
    paused = true
  })

  def gameGoOn(): Unit = enqueueToEngine({
    paused = false
  })

  def gameStop(): Unit = enqueueToEngine(stop())

  override def simpleInitApp(): Unit = {
    assetManager.registerLocator("model", classOf[FileLocator])

    /**
     * 摄像机
     */
    cam.setLocation(new Vector3f(0, 7, 12))
    cam.setRotation(new Quaternion(0f, 0.98957336f, -0.14402872f, 4.8319108E-4f))
    flyCam.setMoveSpeed(10f)
    flyCam.setDragToRotate(true)

    /**
     * 要有光
     */
    rootNode.addLight(new AmbientLight(new ColorRGBA(0.2f, 0.2f, 0.2f, 1f)))
    rootNode.addLight(new DirectionalLight(new Vector3f(-1, -2, -3), new ColorRGBA(0.8f, 0.8f, 0.8f, 1f)))

    inputManager.addMapping(KeyR, new KeyTrigger(KeyInput.KEY_R))
    inputManager.addMapping(KeyT, new KeyTrigger(KeyInput.KEY_T))
    inputManager.addListener(actionListener, KeyR, KeyT)

  }

  private val actionListener = new ActionListener() {
    override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {
      if (isPressed) name match {
        case KeyR =>
          val location = cam.getLocation
          val rotation = cam.getRotation
          println(location + "," + rotation)
        case KeyT =>
          if(flyCam.isDragToRotate){
            flyCam.setDragToRotate(false)
          }else{
            flyCam.setDragToRotate(true)
          }
        case _ =>
      }
    }
  }

  /**
    * @param fun function push into engine thread
    **/
  def enqueueToEngine(fun: => Unit): Unit = enqueue(new Runnable {
    override def run(): Unit = fun
  })

}
