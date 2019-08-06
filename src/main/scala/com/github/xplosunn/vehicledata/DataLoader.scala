package com.github.xplosunn.vehicledata

import java.time.Instant

import com.github.xplosunn.vehicledata.model._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

object DataLoader {
  def main(args: Array[String]): Unit = {
    val filePath = args.last

    val db = Database
      .forURL("jdbc:postgresql://localhost:5432/", user = "postgres", driver = "org.postgresql.ds.PGSimpleDataSource")
    Await.result(db.run(createTablesSql.as[Int]), Duration.Inf)
    val busGpsDataTable = TableQuery[BusGpsDataTable]

    Source.fromFile(filePath).getLines().foreach { line =>
      val commaSeparatedValues = line.split(',')

      val timestamp = Instant.ofEpochMilli(0).plusNanos(commaSeparatedValues(0).toLong * 1000)
      val operator  = Operator(commaSeparatedValues(6))
      val lon       = Lon(BigDecimal(commaSeparatedValues(7)))
      val lat       = Lat(BigDecimal(commaSeparatedValues(8)))
      val vehicleId = VehicleId(commaSeparatedValues(11))
      val atStop    = commaSeparatedValues(11).toInt != 0

      val insertResult = db.run(busGpsDataTable += BusGpsData(timestamp, operator, lon, lat, vehicleId, atStop))
      if (Await.result(insertResult, Duration.Inf) != 1) {
        throw new IllegalStateException("Failed to insert a row")
      }
    }
  }

  val createTablesSql =
    sql"""
         CREATE TABLE IF NOT EXISTS bus_gps_data (
            "timestamp" timestamp NOT NULL,
            operator varchar(255) NOT NULL,
            lon numeric(13,10) NOT NULL,
            lat numeric(12,10) NOT NULL,
            vehicle_id varchar(255) NOT NULL,
            at_stop bool NOT NULL,
            PRIMARY KEY(timestamp, operator, vehicle_id)
         );
         SELECT 1;
       """
}
