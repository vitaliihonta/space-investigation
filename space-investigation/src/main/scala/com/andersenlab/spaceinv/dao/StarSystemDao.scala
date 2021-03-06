package com.andersenlab.spaceinv.dao

import java.util.UUID

import cats.data.NonEmptyList
import com.andersenlab.spaceinv.api.modelView.{PlanetView, StarSystemView, StarView}
import com.andersenlab.spaceinv.api.request.CreateStarSystemRequest
import com.andersenlab.spaceinv.dao.ExtPostgresProfile.api._
import com.andersenlab.spaceinv.model._
import slick.dbio.Effect
import slick.sql.SqlAction

import scala.concurrent.ExecutionContext

trait StarSystemDao {
  def saveDetailStarSystem(detailStarSystem: CreateStarSystemRequest): DBIO[Unit]

  def updateStarSystem(starSystem: StarSystem): DBIO[Int]

  def saveStarSystem(starSystem: StarSystem): DBIO[Int]

  def findStarSystem(starSystemId: UUID): DBIO[Option[StarSystemView]]

  def listAll(): DBIO[List[StarSystem]]
}

class StarSystemDaoImpl(implicit ec: ExecutionContext) extends StarSystemDao {

  override def findStarSystem(starSystemId: UUID): DBIO[Option[StarSystemView]] = {

    val starSystemDBIO: SqlAction[Option[StarSystem], NoStream, Effect.Read] = StarSystemTable
      .starSystem
      .filter(_.id === starSystemId.bind)
      .result.headOption

    starSystemDBIO.flatMap {
      case None => DBIO.successful(None)
      case Some(starSystem) =>
        for {
          stars <- StarTable.star.filter(_.starSystemId === starSystemId.bind).to[List].result
          planets <- PlanetTable.planet.filter(_.starSystemId === starSystemId.bind).to[List].result
        } yield {
          NonEmptyList.fromList(stars).map { nonEmptyStars =>
            StarSystemView(
              starSystemId,
              starSystem.name,
              nonEmptyStars.map(StarView.fromStar),
              planets.map(PlanetView.fromPlanet)
            )
          }
        }
    }
  }

  override def listAll(): DBIO[List[StarSystem]] = {
    StarSystemTable.starSystem.to[List].result
  }

  override def saveStarSystem(starSystem: StarSystem): DBIO[Int] = {
    StarSystemTable.starSystem += starSystem
  }

  override def updateStarSystem(starSystem: StarSystem): DBIO[Int] = {
    StarSystemTable.starSystem.filter(_.id === starSystem.id).update(starSystem)
  }

  override def saveDetailStarSystem(detailStarSystem: CreateStarSystemRequest): DBIO[Unit] = {


    val starSystemId = UUID.randomUUID()
    val starSystemCreation = StarSystemTable.starSystem += StarSystem(starSystemId, detailStarSystem.name)

    val starCreation = detailStarSystem.stars.map { starRequest =>
      StarTable.star += Star(UUID.randomUUID(),
        starRequest.name,
        starSystemId,
        starRequest.characteristics,
        starRequest.`type`)
    }.toList

    val planetCreation = detailStarSystem.planets.map { planetRequest =>
      PlanetTable.planet += Planet(UUID.randomUUID(),
        planetRequest.name,
        planetRequest.coordinates,
        starSystemId,
        planetRequest.characteristics)
    }.toList


    DBIO.sequence(starCreation ++ planetCreation :+ starSystemCreation)
      .transactionally.map(_ => ())

  }


}
