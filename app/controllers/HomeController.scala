package controllers

import play.api.http.ContentTypes
import play.api.mvc._

import java.nio.charset.StandardCharsets
import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def test() = Action { implicit req =>
    Ok(s"${req.body.asText.get.getBytes(StandardCharsets.UTF_8).mkString}")
  }
}


import akka.util.ByteString

class NullCharFilter extends EssentialFilter {

  import ContentTypes._
  import NullCharFilter._
  import akka.stream.scaladsl.Flow

  private val targets = Seq(JSON, TEXT)
  private val removeFlow = Flow[ByteString].map(removeNullByte)

  override def apply(next: EssentialAction): EssentialAction = (request: RequestHeader) =>
     request.contentType match {
       case Some(tpe) if targets.contains(tpe) => removeFlow ~>: next(request)
        case _                                  => next(request)
      }
  }

object NullCharFilter {
  val NullByte: Byte = 0x00.toByte
  def removeNullByte(src: ByteString): ByteString = src.filter(x => x != NullByte)
}