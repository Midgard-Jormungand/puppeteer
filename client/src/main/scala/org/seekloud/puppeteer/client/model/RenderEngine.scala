package org.seekloud.puppeteer.client.model

import com.jme3.app.SimpleApplication
import com.jme3.asset.plugins.FileLocator
import com.jme3.light.{AmbientLight, DirectionalLight}
import com.jme3.math.{ColorRGBA, Quaternion, Vector3f}

/**
  * Created by sky
  * Date on 2019/9/25
  * Time at 下午5:03
  * render2memory
  */
object RenderEngine extends SimpleApplication {
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
    cam.setLocation(new Vector3f(8.896082f, 12.328749f, 13.69658f))
    cam.setRotation(new Quaternion(-0.09457599f, 0.9038204f, -0.26543108f, -0.32204098f))
    flyCam.setMoveSpeed(10f)

    /**
     * 要有光
     */
    rootNode.addLight(new AmbientLight(new ColorRGBA(0.2f, 0.2f, 0.2f, 1f)))
    rootNode.addLight(new DirectionalLight(new Vector3f(-1, -2, -3), new ColorRGBA(0.8f, 0.8f, 0.8f, 1f)))

  }

  /**
    * @param fun function push into engine thread
    **/
  def enqueueToEngine(fun: => Unit): Unit = enqueue(new Runnable {
    override def run(): Unit = fun
  })

}
