package com.github.xplosunn.vehicledata

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.instances.future._
import com.github.xplosunn.vehicledata.controller.{ToFuture, VehicleDataController}
import com.github.xplosunn.vehicledata.model.BusGpsDataTable
import com.github.xplosunn.vehicledata.repository.BusGpsDataRepositoryImpl
import com.github.xplosunn.vehicledata.service.VehicleDataService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Starter {
  def main(args: Array[String]): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

    implicit val ec = actorSystem.dispatcher

    import slick.jdbc.PostgresProfile.api._

    val database = Database
      .forURL("jdbc:postgresql://localhost:5432/", user = "postgres", driver = "org.postgresql.ds.PGSimpleDataSource")
    val busGpsDataTable       = TableQuery[BusGpsDataTable]
    val vehicleDataRepository = new BusGpsDataRepositoryImpl(database, busGpsDataTable)

    val vehicleDataService = new VehicleDataService(vehicleDataRepository)

    implicit val toFuture     = ToFuture[Future]
    val vehicleDataController = new VehicleDataController(vehicleDataService)

    Await.result(Http().bindAndHandle(vehicleDataController.route, "0.0.0.0", 9000), Duration.Inf)
    ()
  }
}
