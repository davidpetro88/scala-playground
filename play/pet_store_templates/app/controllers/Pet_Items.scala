package controllers


import javax.inject.Singleton

import models.Pet_Item
import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.libs.json.{__, Reads}
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import play.api.libs.json._

@Singleton
class Pet_Items extends Controller {

  val petShop = models.Pet_Shop


  // Json serializers
  implicit val readsPet_Item = Json.reads[Pet_Item]

  implicit val pet_ItemWrites = new Writes[Pet_Item] {
    def writes(pet: Pet_Item) = Json.obj(
      "id" -> pet.id,
      "name" -> pet.name,
      "price" -> pet.price
    )
  }

 // val list = Action { NotImplemented  }

 // val list = Action { Ok( "List of results to come") }

 // val list = Action { Ok(Json.obj( "result" -> "undefined", "message" -> "list results not implemented yet" ))}

  val list = Action { implicit request =>

    render {
      case Accepts.Json() => Ok(Json.toJson(petShop.list))
      case Accepts.Html() => Ok(views.html.list(petShop.list))

      case emptyresult => NotFound
    }

  }


  val create =  Action(parse.json) { implicit request =>
    request.body.validate[Pet_Item] match {
      case JsSuccess(createItem, _) =>
        petShop.create(createItem.name, createItem.price) match {
          case Some(item) => Ok(Json.toJson(item))
          case None => InternalServerError
        }
      case JsError(errors) =>
        BadRequest
    }
  }

  def details(id: Long) = Action  {  implicit request =>

   render {
        case Accepts.Html() => Ok(views.html.details(petShop.details(id).get))
        case emptyresult => NotFound
      }

    }


  def update(id: Long) =  Action { NotImplemented }

  def delete(id: Long) = Action { NotImplemented }


}

