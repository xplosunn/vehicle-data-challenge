package com.github.xplosunn.vehicledata.api

import com.github.xplosunn.vehicledata.repository.BusGpsDataRepository

final case class VehicleRoute(route: Seq[BusGpsDataRepository.GpsData])
