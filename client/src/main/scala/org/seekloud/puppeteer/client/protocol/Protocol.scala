package org.seekloud.puppeteer.client.protocol
import com.jme3.math.{FastMath, Quaternion, Vector3f}

object Protocol {
  case class Vec3f(x: Float, y: Float, z: Float){
    def -(other: Vec3f): Vec3f = Vec3f(x - other.x, y - other.y, z - other.z)
    def module: Float = FastMath.sqrt(x * x + y * y + z * z)
    def dot(other: Vec3f): Float = x * other.x + y * other.y + z * other.z
    def angleBetween(other: Vec3f): Float = FastMath.acos(dot(other)/(module * other.module))
    def cross(other: Vec3f): Vec3f = {
      val otherX = other.x
      val otherY = other.y
      val otherZ = other.z
      Vec3f((y * otherZ) - (z * otherY), (z * otherX) - (x * otherZ), (x * otherY) - (y * otherX))
    }
    def toJmeVec: Vector3f = new Vector3f(x, y, z)
    def rotateFromThisToOther(other: Vec3f): Quaternion = {
      val angle = angleBetween(other)
      val axis = cross(other).toJmeVec
      new Quaternion().fromAngleAxis(angle, axis)
    }
  }
}
