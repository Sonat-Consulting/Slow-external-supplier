package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import java.util.concurrent.atomic.AtomicInteger

object Application extends Controller {

  val nextOrderId = new AtomicInteger(1000)

  case class OrderReference(orderId:String, itemId:String)

  implicit val jsonOrderReference = Json.format[OrderReference]

  def placeOrder(itemId:String) = Action {

    val nextId = nextOrderId.getAndIncrement
    val orderRef = OrderReference(""+nextId, itemId)
    Logger.info(s"Placing order $orderRef")

    val json = Json.toJson(orderRef)

    Ok(json)
  }

}