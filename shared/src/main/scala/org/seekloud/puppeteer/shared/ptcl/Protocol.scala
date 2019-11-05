package org.seekloud.puppeteer.shared.ptcl

/**
  * Created by haoshuhan on 2019/5/14.
  */
object Protocol {
  case class PointList(data: List[Point])
  case class Point(x: Float, y: Float, z: Float){
    def sub(point: Point) :Point = {
      Point(x - point.x, y-point.y,z-point.z)
    }
  }

}
