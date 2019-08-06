package com.github.xplosunn.vehicledata.repository

import java.time.Instant

import cats.Id
import com.github.xplosunn.vehicledata.model.{BusGpsData, Operator, VehicleId}
import com.github.xplosunn.vehicledata.repository.BusGpsDataRepository.GpsData

import scala.collection.SeqView
import scala.collection.mutable.MutableList

class FakeBusGpsDataRepository extends BusGpsDataRepository[Id] {

  val underlying: MutableList[BusGpsData] = MutableList.empty

  def operators(startTime: Instant, endTime: Instant): Seq[Operator] =
    between(startTime, endTime).map(_.operator).to[List].distinct

  def vehicles(startTime: Instant, endTime: Instant, operator: Operator, atStop: Option[Boolean]): Seq[VehicleId] =
    between(startTime, endTime)
      .filter(row => row.operator == operator && atStop.forall(_ == row.atStop))
      .map(_.vehicleId)
      .to[List]
      .distinct

  def vehicleRoute(startTime: Instant, endTime: Instant, vehicleId: VehicleId): Seq[GpsData] =
    between(startTime, endTime).filter(_.vehicleId == vehicleId).map(row => GpsData(row.lat, row.lon)).toList

  private def between(startTime: Instant, endTime: Instant): SeqView[BusGpsData, MutableList[BusGpsData]] =
    underlying.view.filter { row =>
      !row.timestamp.isBefore(startTime) &&
      !row.timestamp.isAfter(endTime)
    }
}
