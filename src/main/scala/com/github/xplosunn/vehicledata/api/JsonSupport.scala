package com.github.xplosunn.vehicledata.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.xplosunn.vehicledata.model.{Lat, Lon, Operator, VehicleId}
import com.github.xplosunn.vehicledata.repository.BusGpsDataRepository.GpsData
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsValue}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val apiErrorFormat = jsonFormat1(ApiError)

  implicit val operatorFormat = rootJsonFormat[Operator](
    (json: JsValue) => Operator(StringJsonFormat.read(json)),
    (obj: Operator) => StringJsonFormat.write(obj.value)
  )
  implicit val operatorsFormat = jsonFormat1(Operators)

  implicit val latFormat = rootJsonFormat[Lat](
    {
      case JsNumber(value) => Lat(value)
      case _               => throw new DeserializationException("lat should be a number")
    },
    (obj: Lat) => JsNumber(obj.value)
  )

  implicit val lonFormat = rootJsonFormat[Lon](
    {
      case JsNumber(value) => Lon(value)
      case _               => throw new DeserializationException("lon should be a number")
    },
    (obj: Lon) => JsNumber(obj.value)
  )

  implicit val gpsDataFormat      = jsonFormat2(GpsData)
  implicit val vehicleRouteFormat = jsonFormat1(VehicleRoute)

  implicit val vehicleIdFormat = rootJsonFormat[VehicleId](
    (json: JsValue) => VehicleId(StringJsonFormat.read(json)),
    (obj: VehicleId) => StringJsonFormat.write(obj.value)
  )
  implicit val vehiclesFormat = jsonFormat1(Vehicles)
}
