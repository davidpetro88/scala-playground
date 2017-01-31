import models.Pet_Item
import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
;

/**
 * Created by ubu on 12.04.16.
 */
class JsonSerdeSpec extends PlaySpec with OneAppPerTest {

    "Pet_Items" should {

        //    {"id" : 42, "price" : 4.2, "name" : "Giant Rabbit"}
        val mockJson = Json.obj( "id" -> 42,  "price" -> 4.2,  "name" -> "Giant Rabbit" )


        "render the pets json serde" in {
            val pets = route(app, FakeRequest(POST, "/pets" ).withJsonBody(mockJson)).get
            status(pets) === OK
            contentType(pets) mustBe Some("application/json")
            val jsonResult =  contentAsJson(pets)


            jsonResult.toString()  === """{"id" : 42, "price" : 4.2, "name" : "Giant Rabbit"}"""
        }


    }





}
