package com.github.xplosunn.vehicledata.repository

import java.time.Instant

import com.github.xplosunn.vehicledata.model._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class BusGpsDataRepositoryImpl(db: Database, table: TableQuery[BusGpsDataTable])
    extends BusGpsDataRepository[Future]
    with BusGpsDataColumnMappings {
  override def operators(startTime: Instant, endTime: Instant): Future[Seq[Operator]] =
    db.run {
      table
        .filter(betweenStartAndEndTime(_, startTime, endTime))
        .map(_.operator)
        .distinct
        .result
    }

  override def vehicles(
    startTime: Instant,
    endTime: Instant,
    operator: Operator,
    atStop: Option[Boolean]
  ): Future[Seq[VehicleId]] =
    db.run {
      table
        .filter(betweenStartAndEndTime(_, startTime, endTime))
        .filter(
          row =>
            row.operator === operator
            && row.atStop === atStop.getOrElse(true)
        )
        .map(_.vehicleId)
        .distinct
        .result
    }

  override def vehicleRoute(
    startTime: Instant,
    endTime: Instant,
    vehicleId: VehicleId
  ): Future[Seq[BusGpsDataRepository.GpsData]] =
    db.run {
      table
        .filter(betweenStartAndEndTime(_, startTime, endTime))
        .filter(row => row.vehicleId === vehicleId)
        .map(row => (row.lat, row.lon).mapTo[BusGpsDataRepository.GpsData])
        .result
    }

  private def betweenStartAndEndTime(row: BusGpsDataTable, startTime: Instant, endTime: Instant) =
    row.timestamp >= startTime && row.timestamp <= endTime

}
