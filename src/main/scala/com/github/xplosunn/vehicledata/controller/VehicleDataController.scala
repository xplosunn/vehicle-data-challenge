package com.github.xplosunn.vehicledata.controller

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, path, _}
import akka.http.scaladsl.server.{Route, StandardRoute}
import cats.Functor
import cats.syntax.functor._
import com.github.xplosunn.vehicledata.api._
import com.github.xplosunn.vehicledata.model.{Operator, VehicleId}
import com.github.xplosunn.vehicledata.service.VehicleDataService

class VehicleDataController[F[_]: Functor: ToFuture](vehicleDataService: VehicleDataService[F]) extends JsonSupport {

  val route: Route = get {
    pathPrefix("api") {
      concat(
        pathPrefix("operator") {
          concat(
            pathEnd {
              withMandatoryTimestampParameters { (start, end) =>
                operators(start, end)
              }
            },
            path(Segment.map(Operator.apply) / "vehicle") { operator =>
              withMandatoryTimestampParameters { (start, end) =>
                parameters('stop.as[Boolean].?) { atStop =>
                  vehicles(start, end, operator, atStop)
                }
              }
            }
          )
        },
        path("vehicle" / Segment.map(VehicleId.apply) / "route") { vehicleId =>
          withMandatoryTimestampParameters { (start, end) =>
            vehicleRoute(start, end, vehicleId)
          }
        }
      )
    }
  }

  private def withMandatoryTimestampParameters[T](route: (Instant, Instant) => Route): Route = {
    def toInstant(value: Long): Instant =
      Instant.ofEpochMilli(0).plus(value, ChronoUnit.MICROS)

    parameters('start.as[Long].?) { startOpt =>
      parameters('end.as[Long].?) { endOpt =>
        (for {
          start <- startOpt
          end   <- endOpt
        } yield {
          if (start <= end) {
            route(toInstant(start), toInstant(end))
          } else {
            complete(StatusCodes.BadRequest -> ApiError("End time should be after start time"))
          }
        }).getOrElse(complete(StatusCodes.BadRequest -> ApiError("start and end query parameters need to be provided")))
      }
    }
  }

  private def operators(startTime: Instant, endTime: Instant) = {
    val operators = vehicleDataService.operators(startTime, endTime)
    completeWith(operators.map(Operators.apply))
  }

  private def vehicles(startTime: Instant, endTime: Instant, operator: Operator, atStop: Option[Boolean]) = {
    val vehicles = vehicleDataService.vehicles(startTime, endTime, operator, atStop)
    completeWith(vehicles.map(Vehicles.apply))
  }

  private def vehicleRoute(startTime: Instant, endTime: Instant, vehicleId: VehicleId) = {
    val route = vehicleDataService.vehicleRoute(startTime, endTime, vehicleId)
    completeWith(route.map(VehicleRoute.apply))
  }

  @inline
  private def completeWith[T](t: F[T])(implicit trm: ToResponseMarshaller[T]): StandardRoute =
    complete(ToFuture[F].apply(t))

}
