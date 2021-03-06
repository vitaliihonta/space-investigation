package com.andersenlab.spaceinv.api.service

import java.util.UUID

import akka.http.scaladsl.server.directives.OnSuccessMagnet
import com.andersenlab.spaceinv.api.modelView.StarSystemView
import com.andersenlab.spaceinv.api.request.CreateStarSystemRequest
import com.andersenlab.spaceinv.dao.ExtPostgresProfile.api.Database
import com.andersenlab.spaceinv.dao.StarSystemDao
import com.andersenlab.spaceinv.model.StarSystem

import scala.concurrent.{ExecutionContext, Future}

trait StarSystemService {
  def saveDetailStarSystem(detailStarSystem: CreateStarSystemRequest): Future[Unit]

  def updateStarSystem(starSystem: StarSystem): Future[Unit]

  def saveStarSystem(starSystem: StarSystem): Future[Unit]

  def findStarSystem(starSystemId: UUID): Future[Option[StarSystemView]]

  def listAll(): Future[List[StarSystem]]
}

class StarSystemServiceImpl(db: Database,
                            starSystemDao: StarSystemDao)(implicit ec: ExecutionContext) extends StarSystemService {
  override def findStarSystem(starSystemId: UUID): Future[Option[StarSystemView]] = {
    db.run {
      starSystemDao.findStarSystem(starSystemId)
    }
  }

  override def listAll(): Future[List[StarSystem]] = {
    db.run {
      starSystemDao.listAll
    }
  }

  override def saveStarSystem(starSystem: StarSystem): Future[Unit] = {
    db.run {
      starSystemDao.saveStarSystem(starSystem).map(_ => ())
    }
  }

  override def updateStarSystem(starSystem: StarSystem): Future[Unit] = {
    db.run {
      starSystemDao.updateStarSystem(starSystem).map(_ => ())
    }
  }

  override def saveDetailStarSystem(detailStarSystem: CreateStarSystemRequest): Future[Unit] = {
    db.run {
      starSystemDao.saveDetailStarSystem(detailStarSystem)
    }
  }
}
