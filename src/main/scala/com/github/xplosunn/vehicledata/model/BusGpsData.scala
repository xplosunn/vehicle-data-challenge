package com.github.xplosunn.vehicledata.model

import java.time.Instant

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final case class BusGpsData(
  timestamp: Instant, // 'Timestamp micro since 1970 01 01 00:00:00 GMT
  // 'Line ID
  // 'Direction
  // 'Journey Pattern ID'
  // Time Frame (The start date of the production time table -
  //                                                - in Dublin the production time table starts at 6am and ends at 3am)
  // 'Vehicle Journey ID (A given run on the journey pattern)
  operator: Operator, // 'Operator (Bus operator, not the driver)
  // 'Congestion [0=no,1=yes]
  lon: Lon, // 'Lon WGS84
  lat: Lat, // 'Lat WGS84
  // 'Delay (seconds, negative if bus is ahead of schedule)
  // 'Block ID (a section ID of the journey pattern)
  vehicleId: VehicleId, // 'Vehicle ID
  // 'Stop ID
  atStop: Boolean // 'At Stop [0=no,1=yes]
)
// 1356998403000000,747,0,07470001,2012-12-31,3493,SL,0,-6.236852,53.425327,-709,747006,40040,7411,0

final case class Operator(value: String)  extends AnyVal
final case class Lat(value: BigDecimal)   extends AnyVal
final case class Lon(value: BigDecimal)   extends AnyVal
final case class VehicleId(value: String) extends AnyVal

trait BusGpsDataColumnMappings {
  implicit val operatorMapping = MappedColumnType.base[Operator, String](
    _.value,
    Operator.apply
  )

  implicit val vehicleIdMapping = MappedColumnType.base[VehicleId, String](
    _.value,
    VehicleId.apply
  )

  implicit val lonMapping = MappedColumnType.base[Lon, BigDecimal](
    _.value,
    Lon.apply
  )

  implicit val latMapping = MappedColumnType.base[Lat, BigDecimal](
    _.value,
    Lat.apply
  )
}

final class BusGpsDataTable(tag: Tag) extends Table[BusGpsData](tag, "bus_gps_data") with BusGpsDataColumnMappings {
  def timestamp: Rep[Instant]   = column[Instant]("timestamp")
  def operator: Rep[Operator]   = column[Operator]("operator")
  def lon: Rep[Lon]             = column[Lon]("lon")
  def lat: Rep[Lat]             = column[Lat]("lat")
  def vehicleId: Rep[VehicleId] = column[VehicleId]("vehicle_id")
  def atStop: Rep[Boolean]      = column[Boolean]("at_stop")

  // scalastyle:off
  def * : ProvenShape[BusGpsData] =
    (timestamp, operator, lon, lat, vehicleId, atStop).mapTo[BusGpsData]
  // scalastyle:on
}
