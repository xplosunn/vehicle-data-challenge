package com.github.xplosunn.vehicledata.repository

import java.time.Instant

import com.github.xplosunn.vehicledata.model.{Lat, Lon, Operator, VehicleId}
import com.github.xplosunn.vehicledata.repository.BusGpsDataRepository.GpsData

trait BusGpsDataRepository[F[_]] {
  def operators(startTime: Instant, endTime: Instant): F[Seq[Operator]]

  def vehicles(
    startTime: Instant,
    endTime: Instant,
    operator: Operator,
    atStop: Option[Boolean]
  ): F[Seq[VehicleId]]

  def vehicleRoute(startTime: Instant, endTime: Instant, vehicleId: VehicleId): F[Seq[GpsData]]

}

object BusGpsDataRepository {

  final case class GpsData(lat: Lat, lon: Lon)

}
