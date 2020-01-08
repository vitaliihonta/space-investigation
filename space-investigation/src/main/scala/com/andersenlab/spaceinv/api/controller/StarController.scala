package com.andersenlab.spaceinv.api.controller

import java.util.UUID

import akka.http.scaladsl.server.Route
import com.andersenlab.spaceinv.api.service.StarService
import com.andersenlab.spaceinv.model.Star

class StarController(starService: StarService) extends ControllerBase {

  override def route: Route = {
    apiV1 {
      pathPrefix("stars") {
        path("list") {
          pathEndOrSingleSlash {
            listAll()
          }
        } ~ path(JavaUUID) { starId =>
          pathEndOrSingleSlash {
            findStarRoute(starId)
          }
        } ~ pathEndOrSingleSlash {
          saveStarRoute() ~
            updateStarRoute()
        }
      }
    }
  }

  def listAll(): Route = {
    get {
      onSuccess(starService.listAll()) { list =>
        complete(list mkString "\n")
      }
    }
  }

  def findStarRoute(starId: UUID): Route = {
    get {
      onSuccess(starService.findStarById(starId)) {
        case None => complete("No Founde")
        case Some(star) => complete(star.toString)

      }
    }
  }

  def saveStarRoute(): Route = {
    post {
      entity(as[Star]) { star =>
        onSuccess(starService.save(star)) {
          complete("success created star")
        }
      }
    }
  }

  def updateStarRoute(): Route = {
    put {
      entity(as[Star]) { star =>
        onSuccess(starService.update(star)) {
          complete("success updated star")
        }
      }
    }
  }
}
