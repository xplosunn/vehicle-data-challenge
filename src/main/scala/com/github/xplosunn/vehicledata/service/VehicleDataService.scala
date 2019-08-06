package com.github.xplosunn.vehicledata.service

import java.time.Instant

import com.github.xplosunn.vehicledata.model.{Operator, VehicleId}
import com.github.xplosunn.vehicledata.repository.BusGpsDataRepository

class VehicleDataService[F[_]](busGpsDataRepository: BusGpsDataRepository[F]) {

  val operators: (Instant, Instant) => F[Seq[Operator]] =
    busGpsDataRepository.operators

  val vehicles: (Instant, Instant, Operator, Option[Boolean]) => F[Seq[VehicleId]] =
    busGpsDataRepository.vehicles

  val vehicleRoute: (Instant, Instant, VehicleId) => F[Seq[BusGpsDataRepository.GpsData]] =
    busGpsDataRepository.vehicleRoute
}
