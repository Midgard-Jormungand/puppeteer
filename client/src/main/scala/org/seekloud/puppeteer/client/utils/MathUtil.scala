package org.seekloud.puppeteer.client.utils

import com.jme3.math.{FastMath, Quaternion, Vector3f}

object MathUtil {
  case class Vec3f(x: Float, y: Float, z: Float){
    // 差
    def -(other: Vec3f): Vec3f = Vec3f(x - other.x, y - other.y, z - other.z)
    // 模
    def module: Float = FastMath.sqrt(x * x + y * y + z * z)
    // 点积
    def dot(other: Vec3f): Float = x * other.x + y * other.y + z * other.z
    // 叉积
    def cross(other: Vec3f): Vec3f = {
      val otherX = other.x
      val otherY = other.y
      val otherZ = other.z
      Vec3f((y * otherZ) - (z * otherY), (z * otherX) - (x * otherZ), (x * otherY) - (y * otherX))
    }
    // 与另一向量夹角
    def angleBetween(other: Vec3f): Float = FastMath.acos(dot(other)/(module * other.module))
    // 转 jme3 Vector3f
    def toJmeVec: Vector3f = new Vector3f(x, y, z)
  }

  // 从一向量到另一向量的四元数旋转角度
  def fromToRotation(fromVec: Vec3f, toVec:Vec3f): Quaternion = {
    val angle = fromVec.angleBetween(toVec)
    val axis = fromVec.cross(toVec).toJmeVec
    new Quaternion().fromAngleAxis(angle, axis)
  }

  // 从(0,1,0)到另一向量的四元数旋转角度
  def fromUnitYToRotation(toVec: Vec3f): Quaternion = {
    val angle = FastMath.acos(toVec.y / toVec.module)
    val axis = new Vector3f(toVec.z, 0, -toVec.x)
    new Quaternion().fromAngleAxis(angle, axis)
  }
}
