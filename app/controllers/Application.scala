package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import java.util.concurrent.atomic.AtomicInteger
import akka.actor.{Props, Actor}
import scala.concurrent.duration.{FiniteDuration, Duration}
import play.api.libs.concurrent.Akka
import akka.pattern.ask
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.util.Random

object Application extends Controller {
  import play.api.Play.current
  import scala.concurrent.ExecutionContext.Implicits.global

  val nextOrderId = new AtomicInteger(1000)

  case class OrderReference(orderId:String, itemId:String)

  implicit val jsonOrderReference = Json.format[OrderReference]

  implicit val timeout = Timeout(60, TimeUnit.MINUTES)

  val delayer = Akka.system.actorOf(Props[DelayActor])

  val random = new Random()


  def shouldFail(errorRateInPercent:Option[Int]):Boolean = {
    val value = random.nextInt(99)
    val percent = errorRateInPercent.getOrElse(30)

    return value < percent
  }

  def getRandom(v:Int):Int = {
    if ( v <= 0 ) {
      v
    } else {
      random.nextInt(v)
    }
  }

  def getFruits(name:String, fixedDelayInMills:Option[Int], randomDelayInMills:Option[Int], errorRateInPercent:Option[Int]):Action[AnyContent] = {
    getFruitsWithAmount(name, 1, fixedDelayInMills, randomDelayInMills, errorRateInPercent)
  }

  def translate(name:String):String = {
    name.toLowerCase match {
      case "orange" => "appelsin"
      case x:String => x
    }
  }

  def getFruitsWithAmount(name:String, amount:Int, fixedDelayInMills:Option[Int], randomDelayInMills:Option[Int], errorRateInPercent:Option[Int]) = Action.async {

    val randomDelay = getRandom(randomDelayInMills.getOrElse(1000))
    val delay = Duration(fixedDelayInMills.getOrElse(1000) + randomDelay, TimeUnit.MILLISECONDS)
    Logger.info(s"Going to return fruits of $name in $delay")

    ask(delayer, delay).map {
      i =>

        if (shouldFail(errorRateInPercent)) {
          Logger.info(s"Decided to fail getting fruits of type $name")
          new Status(SERVICE_UNAVAILABLE)
        } else {

          Logger.info(s"Returning fruits of type $name")

          val norwegianName:String = translate(name)
          Logger.info(s"norwegianName: $norwegianName")
          val list = Range(0,amount).map( x => norwegianName).toList


          val json = Json.toJson(list)

          Ok(json)
        }
    }
  }

  def placeOrder(itemId:String, fixedDelayInMills:Option[Int], randomDelayInMills:Option[Int], errorRateInPercent:Option[Int]) = Action.async {

    val randomDelay = getRandom(randomDelayInMills.getOrElse(1000))
    val delay = Duration(fixedDelayInMills.getOrElse(1000) + randomDelay, TimeUnit.MILLISECONDS)
    Logger.info(s"Going to Place order for $itemId in $delay")

    ask(delayer, delay).map {
      i =>

        if (shouldFail(errorRateInPercent)) {
          Logger.info(s"Decided to fail Placing order for $itemId")
          new Status(SERVICE_UNAVAILABLE)
        } else {
          val nextId = nextOrderId.getAndIncrement
          val orderRef = OrderReference(""+nextId, itemId)
          Logger.info(s"Placing order $orderRef")

          val json = Json.toJson(orderRef)

          Ok(json)
        }
    }
  }

  class DelayActor extends Actor {

    def receive = {
      case d: Duration => {
        val s = sender
        context.system.scheduler.scheduleOnce(FiniteDuration(d.length, d.unit)){
          s ! "done"
        }
      }
    }

  }
}