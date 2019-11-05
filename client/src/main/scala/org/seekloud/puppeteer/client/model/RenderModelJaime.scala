package org.seekloud.puppeteer.client.model

import com.jme3.animation._
import com.jme3.math.{Quaternion, Vector3f}
import com.jme3.math.FastMath
import com.jme3.scene.Node

/**
  * Created by sky
  * Date on 2019/9/26
  * Time at 下午7:28
  */
object RenderModelJaime extends RenderModel {
  override val id: Byte = 1
  protected var model:Node = null
  private var ske: Skeleton = null
  private var head: Bone = null
  private var neck: Bone = null
  private var ribs: Bone = null
  private var spine: Bone = null
  private var hips: Bone = null
  private var shoulderL: Bone = null
  private var upperArmL: Bone = null
  private var forearmL: Bone = null
  private var shoulderR: Bone = null
  private var upperArmR: Bone = null
  private var forearmR: Bone = null
  private var thighL: Bone = null
  private var shinL: Bone = null
  private var thighR: Bone = null
  private var shinR: Bone = null

  RenderEngine.enqueueToEngine({

    /**
      * 加载Jaime的模型
      */
    // 我们的模特：Jaime
    model = RenderEngine.getAssetManager.loadModel("Models/Jaime/Jaime.j3o").asInstanceOf[Node]
    // 将Jaime放大一点点，这样我们能观察得更清楚。
    model.scale(5f)

    // 获得SkeletonControl
    // 骨骼控制器
    val sc = model.getControl(classOf[SkeletonControl])
    ske = sc.getSkeleton
//    println(ske)
    head = ske.getBone("head")
    head.setUserControl(true)
    neck = ske.getBone("neck")
    neck.setUserControl(true)
    ribs = ske.getBone("ribs")
    ribs.setUserControl(true)
    spine = ske.getBone("spine")
    spine.setUserControl(true)
    hips = ske.getBone("hips")
    hips.setUserControl(true)
    shoulderL = ske.getBone("shoulder.L")
    shoulderL.setUserControl(true)
    upperArmL = ske.getBone("upper_arm.L")
    upperArmL.setUserControl(true)
    forearmL = ske.getBone("forearm.L")
    forearmL.setUserControl(true)
    shoulderR = ske.getBone("shoulder.R")
    shoulderR.setUserControl(true)
    upperArmR = ske.getBone("upper_arm.R")
    upperArmR.setUserControl(true)
    forearmR = ske.getBone("forearm.R")
    forearmR.setUserControl(true)
    thighL = ske.getBone("thigh.L")
    thighL.setUserControl(true)
    shinL = ske.getBone("shin.L")
    shinL.setUserControl(true)
    thighR = ske.getBone("thigh.R")
    thighR.setUserControl(true)
    shinR = ske.getBone("shin.R")
    shinR.setUserControl(true)

  })

  override def setModel(): Unit = {
    RenderEngine.getRootNode.detachAllChildren()

    RenderEngine.getRootNode.attachChild(model)
  }

  override def rightUpperArmChange(x: Float, y: Float, z: Float): Unit = {
//    val initVector = (0,1,0)
    val angle = Math.acos(y / Math.sqrt(x * x + y * y + z * z)).toFloat
    val axis = new Vector3f(z,0,-x)
    val move = new Quaternion().fromAngleAxis(angle, axis)
//    println(axis)
//    println(angle)
    upperArmR.setUserTransforms(Vector3f.ZERO, move, Vector3f.UNIT_XYZ)
    ske.updateWorldVectors()
  }

  override def rightForearmChange(forearmVector: Vector3f = Vector3f.ZERO, upperArmVector: Vector3f = Vector3f.ZERO): Unit = {
    forearmVector.normalizeLocal()
    upperArmVector.normalizeLocal()
    val angle = forearmVector.angleBetween(upperArmVector)
    val axis = upperArmVector.cross(forearmVector)
    val move = new Quaternion().fromAngleAxis(angle, axis)
//    val move = new Quaternion().fromAngleAxis(FastMath.PI/4, Vector3f.UNIT_Z)
    //    println(axis)
    //    println(angle)
    forearmR.setUserTransforms(Vector3f.ZERO, move, Vector3f.UNIT_XYZ)
    ske.updateWorldVectors()
  }

}
