package org.seekloud.puppeteer.client.model

import com.jme3.math.Vector3f
import com.jme3.scene.Node

/**
 * Created by sky
 * Date on 2019/9/26
 * Time at 下午7:16
 * 模型控制抽象类
 */
object RenderModel{
  def apply(id: Byte): RenderModel = {
    val model= id match {
      case _=> RenderModelJaime
    }
    RenderEngine.gamePause()
    RenderEngine.enqueueToEngine(model.setModel())
    RenderEngine.gameGoOn()
    model
  }
}
trait RenderModel {
  val id:Byte

  protected var model:Node

  def setModel():Unit

  def rightUpperArmChange(x: Float,y: Float, z: Float): Unit

  def rightForearmChange(forearmVector: Vector3f = Vector3f.ZERO, upperArmVector: Vector3f = Vector3f.ZERO): Unit
}

