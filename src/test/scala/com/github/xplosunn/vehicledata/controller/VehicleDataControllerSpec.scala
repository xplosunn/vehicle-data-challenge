package com.github.xplosunn.vehicledata.controller

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.http.scaladsl.testkit.Specs2RouteTest
import com.github.xplosunn.vehicledata.controller.VehicleDataControllerSpec._
import com.github.xplosunn.vehicledata.model._
import com.github.xplosunn.vehicledata.repository.FakeBusGpsDataRepository
import com.github.xplosunn.vehicledata.service.VehicleDataService
import org.specs2.mutable.Specification

class VehicleDataControllerSpec extends Specification with Specs2RouteTest {

  val fakeRepository = new FakeBusGpsDataRepository()
  fakeRepository.underlying ++= testData

  val controller            = new VehicleDataController(new VehicleDataService(fakeRepository))
  val controllerWithoutData = new VehicleDataController(new VehicleDataService(new FakeBusGpsDataRepository()))

  "VehicleDataController" should {
    "api/operator" in {
      val route = Endpoints.operator
      "return empty list" in {
        Get(route + "?start=1&end=1") ~> controllerWithoutData.route ~> check {
          responseAs[String] shouldEqual jsonObjWithEmptyArrayField("operators")
        }
      }
      `return error when start and / or end query parameters are missing`(route)
      `return error when end time is before start time`(route)

      "return multiple" in {
        Get(route + "?start=1&end=10") ~> controller.route ~> check {
          responseAs[String] shouldEqual """{"operators":["1","X"]}"""
        }
      }
    }
    "api/operator/{operatorId}/vehicle" in {
      val route = Endpoints.operatorVehicle
      "return empty list" in {
        Get(route("N") + "?start=1&end=1") ~> controllerWithoutData.route ~> check {
          responseAs[String] shouldEqual jsonObjWithEmptyArrayField("vehicles")
        }
      }
      `return error when start and / or end query parameters are missing`(route("1"))
      `return error when end time is before start time`(route("1"))
      "return multiple" in {
        Get(route("1") + "?start=1&end=10") ~> controller.route ~> check {
          responseAs[String] shouldEqual """{"vehicles":["2","3","4"]}"""
        }
      }
      "return multiple at stop" in {
        Get(route("1") + "?start=1&end=10&stop=true") ~> controller.route ~> check {
          responseAs[String] shouldEqual """{"vehicles":["2","3"]}"""
        }
      }
    }
    "api/vehicle/{vehicleId}/route" in {
      val route = Endpoints.vehicleRoute
      "return empty list" in {
        Get(route + "?start=1&end=1") ~> controllerWithoutData.route ~> check {
          responseAs[String] shouldEqual jsonObjWithEmptyArrayField("route")
        }
      }
      `return error when start and / or end query parameters are missing`(route)
      `return error when end time is before start time`(route)
      "return multiple" in {
        Get(route + "?start=2&end=3") ~> controller.route ~> check {
          responseAs[String] shouldEqual """{"route":[{"lat":20,"lon":11},{"lat":20,"lon":12}]}"""
        }
      }
    }
  }

  private def `return error when start and / or end query parameters are missing`(endpoint: String) =
    "return error when start and / or end query parameters are missing" in {
      List(
        (None, None),
        (Some(1), None),
        (None, Some(1))
      ).map {
          case (startOpt, endOpt) =>
            (startOpt.map("start=" + _).toList ++ endOpt.map("end=" + _).toList)
              .reduceOption(_ + "&" + _)
              .getOrElse("")
        }
        .map { queryParamsStr =>
          Get(endpoint + s"?$queryParamsStr") ~> controller.route ~> check {
            responseAs[String] shouldEqual needToSpecifyStartAndEndTime
          }
        }
    }

  private def `return error when end time is before start time`(endpoint: String) =
    "return error when end time is before start time" in {
      Get(endpoint + s"?start=3&end=2") ~> controller.route ~> check {
        responseAs[String] shouldEqual invalidStartTimeEndTime
      }
    }
}

object VehicleDataControllerSpec {

  object Endpoints {
    val operator        = "/api/operator"
    val operatorVehicle = (operatorId: String) => s"/api/operator/$operatorId/vehicle"
    val vehicleRoute    = "/api/vehicle/2/route"
  }

  val invalidStartTimeEndTime      = """{"error":"End time should be after start time"}"""
  val needToSpecifyStartAndEndTime = """{"error":"start and end query parameters need to be provided"}"""

  def jsonObjWithEmptyArrayField(fieldName: String): String =
    s"""{"$fieldName":[]}"""

  def testData: Seq[BusGpsData] = Seq(
    BusGpsData(
      Instant.ofEpochMilli(0).plus(1, ChronoUnit.MICROS),
      Operator("1"),
      Lon(BigDecimal(10)),
      Lat(BigDecimal(20)),
      VehicleId("2"),
      false
    ),
    BusGpsData(
      Instant.ofEpochMilli(0).plus(2, ChronoUnit.MICROS),
      Operator("1"),
      Lon(BigDecimal(11)),
      Lat(BigDecimal(20)),
      VehicleId("2"),
      false
    ),
    BusGpsData(
      Instant.ofEpochMilli(0).plus(3, ChronoUnit.MICROS),
      Operator("1"),
      Lon(BigDecimal(12)),
      Lat(BigDecimal(20)),
      VehicleId("2"),
      true
    ),
    BusGpsData(
      Instant.ofEpochMilli(0).plus(4, ChronoUnit.MICROS),
      Operator("1"),
      Lon(BigDecimal(12)),
      Lat(BigDecimal(20)),
      VehicleId("3"),
      true
    ),
    BusGpsData(
      Instant.ofEpochMilli(0).plus(4, ChronoUnit.MICROS),
      Operator("1"),
      Lon(BigDecimal(12)),
      Lat(BigDecimal(20)),
      VehicleId("4"),
      false
    ),
    BusGpsData(
      Instant.ofEpochMilli(0).plus(1, ChronoUnit.MICROS),
      Operator("X"),
      Lon(BigDecimal(0)),
      Lat(BigDecimal(0)),
      VehicleId("2"),
      false
    ),
    BusGpsData(
      Instant.ofEpochMilli(0).plus(1, ChronoUnit.MICROS),
      Operator("X"),
      Lon(BigDecimal(1)),
      Lat(BigDecimal(0)),
      VehicleId("2"),
      true
    ),
  )

}
