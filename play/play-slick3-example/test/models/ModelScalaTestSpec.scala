package models

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._
import testhelpers.{EvolutionHelper, Injector}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class ModelScalaTestSpec extends PlaySpec with OneAppPerTest with BeforeAndAfterEach {

  val projectRepo = Injector.inject[ProjectRepo]

  override def afterEach() = EvolutionHelper.clean()

  "An item " should {

    "be inserted during the first test case" in new WithApplication(FakeApplication()) {
        val action = projectRepo.create("A")
          .flatMap(_ => projectRepo.all)

        val result = Await.result(action, Duration.Inf)

        result mustBe List(Project(1, "A"))
    }

    "and not exist in the second test case" in new WithApplication(FakeApplication()) {
        val action = projectRepo.all

        val result = Await.result(action, Duration.Inf)

        result mustBe List.empty
    }


  }

}
