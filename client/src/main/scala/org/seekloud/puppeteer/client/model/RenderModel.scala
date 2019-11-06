package org.seekloud.puppeteer.client.model

import com.jme3.scene.Node
import org.seekloud.puppeteer.client.protocol.Protocol.Vec3f

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

  def upperArmLeftChange(vec: Vec3f): Unit

  def forearmLeftChange(forearmVector: Vec3f, upperArmVector: Vec3f): Unit

  def upperArmRightChange(vec: Vec3f): Unit

  def forearmRightChange(forearmVector: Vec3f, upperArmVector: Vec3f): Unit
}

