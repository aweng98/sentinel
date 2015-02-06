
import com.alertavert.sentinel.errors._
import controllers.{ApiController, AppController}
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.{ExecutionException, Future}

object Global extends GlobalSettings {

  /**
   * This gets invoked when an action causes an exception to be raised.
   * <p>We try to interpret the error that was caused, and provide a suitable response code; the
   * error message is passed in the JSON response, as well as the offending API path request.
   *
   * <p>Essentially this method tries to map our internal application exceptions to the standard
   * set of HTTP error codes / responses, as well as logging (for reporting purposes) the kind of
   * errors encountered.
   *
   * <p>Additionally, this should be the "hook point" for metrics reporting around errors and
   * other possible production issues (eg, DoS attack detection).
   *
   * @param request
   * @param wrappingEx
   * @return
   */
  override def onError(request: RequestHeader, wrappingEx: Throwable) = {
    // strip the outer ExecutionEx
    val ex = wrappingEx.getClass.getName match {
      // TODO: this is a bit hacky, but seems to be the only way to remove a wrapping exception
      // that is also an anonymous class (so, we can't use a better matching clause)
      case "play.api.Application$$anon$1" => wrappingEx.getCause
      case _ => wrappingEx
    }
    val content = Json.parse(
        s"""{
          |   "error": "${ex.getMessage}",
          |   "request": "${request.path}"
          | }
        """.stripMargin
      )
    Logger.error(s"${request.path} - ${ex.getMessage}")
    val result = ex match {
      case e: NotAllowedException => Forbidden(content)
      case e: NotFoundException => NotFound(content)
      case e: PermissionAccessError => Forbidden(content)
      case e: SecurityException => Unauthorized(content)
      case _ => BadRequest(content)
    }
    Future.successful(result)
  }

  /**
   * Invoked on a non-existent API endpoint being requested
   *
   * @param request
   * @return
   */
  override def onHandlerNotFound(request: RequestHeader) = {
    val content = Json.parse(
      s"""{
          |   "error": "The requested endpoint does not exist",
          |   "request": "${request.path}"
          | }
        """.stripMargin
    )
    Logger.error(s"No API endpoint found while processing ${request.path}")
    Future.successful(NotFound(content))
  }

  /**
   * A valid API endpoint was found, but the request parameters could not be bound
   *
   * @param request
   * @param error
   * @return
   */
  override def onBadRequest(request: RequestHeader, error: String) = {
    val content = Json.parse(
      s"""{
          |   "error": "Could not bind the request arguments",
          |   "detail": "${error}",
          |   "request": "${request.path}"
          | }
        """.stripMargin
    )
    Logger.error(s"Error binding parameters for ${request.path}: ${error}")
    Future.successful(BadRequest(content))
  }

  override def onStart(app: Application) {
    Logger.info("Application has started")
    ApiController.initialize()
    AppController.initialize()
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }
}
