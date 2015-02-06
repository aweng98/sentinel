// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package controllers

import com.alertavert.sentinel.model.User
import com.alertavert.sentinel.persistence.mongodb.MongoUserDao
import com.alertavert.sentinel.security.Credentials
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}
import play.Play
import play.api._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.twirl.api.Html

import scala.collection.mutable
import scala.io.Source

/**
 * Main application controller, provides status, health and other Application-related
 * endpoints.
 *
 * It also centralizes the management of all application-wide configuration settings,
 * abstracting away from the actual use of a configuration file (`/conf/application.conf`) or
 * system environment variables.
 *
 * Created by marco on 9/7/14.
 */
trait AppController   {
  this: Controller =>

    /** Approximate time this server started */
    val serverStartedAt = DateTime.now
    val dateFmt = ISODateTimeFormat.dateTimeNoMillis()

    Logger.info("Application Controller started")

    def initialize(): Unit = {
      Logger.info("Initializing application; bootstrapping user DB")
      Logger.debug(s"Bootstrapping from ${configuration.getBootstrapFilepath}")
      val json = Source.fromFile(configuration.getBootstrapFilepath).getLines().mkString
      val users = Json.parse(json).as[List[Map[String, JsValue]]]
      try {
        users.foreach(insertUser(_))
      } catch {
        // we catch, log and ignore the case of incorrectly initialized users
        case ex: IllegalArgumentException => Logger.error(ex.getLocalizedMessage)
        // other unexpected errors must cause a 'fail fast' condition
        case ex: Exception => {
          Logger.error(s"Unexpected error: ${ex.getLocalizedMessage} - aborting execution")
          throw new RuntimeException(ex)
        }
      }
      Logger.debug("Bootstrap complete")
    }

    private def insertUser(usr: Map[String, JsValue]): Unit = {
      // first off extract the credentials and convert into a valid Credentials object
      val creds = usr.getOrElse("credentials",
          throw new IllegalArgumentException(s"No credentials in initialization object: $usr"))

      val username = (creds \ "username").as[String]
      val password = (creds \ "password").as[String]
      val dao = MongoUserDao()

      dao.findByName(username) match {
        case Some(u: User) => {
          Logger.info(s"Updating (if necessary) user $username")
          if (!u.authenticate(username, password)) {
            Logger.info("Resetting password")
            u.resetPassword(password)
          }
          if (!u.isActive) u.activate()
          dao << u
        }
        case None => {
            val user = User.builder(usr.get("first").map(x => x.as[String]).getOrElse(""),
                    usr.get("last").map(x => x.as[String]).getOrElse(""))
              .hasCreds(Credentials(username, password))
              .setActive(true)
              .build()
            Logger.info(s"Inserting new user $user")
            dao << user
        }
      }
    }

    /**
     * Simple health endpoint, returns just a 200 OK and an "Ok" string; useful for monitoring
     *
     * @return a 200 OK response
     */
    def health = Action {
      Ok("Ok")
    }

    /**
     * Status endpoint, reports all sorts of stats about current server status and configuration
     *
     * @return the server status, in JSON format
     */
    def status = Action {
      var stat = Map.empty[String, String]
      // TODO: add more information about server status
      stat += ("status" -> "running")
      stat += ("started_at" -> dateFmt.print(serverStartedAt))

      // TODO: the way Scala handles JSON and maps seems to be idiotic - find out if recursive
      // implementations are available or do we need to hand-craft our own (really?)
      //
      // What I really want to do here is: stat += ("configuration" -> configuration.toMap)
      stat ++= configuration.toMap

      Ok(Json.toJson(stat))
    }

  /**
   * Launches the main UI to interact with the Sentinel UI
   *
   * @return
   */
    def mainUi = Action {
      Ok(views.html.main("Sentinel UI"))
    }


  /**
     * Application configuration values, abstracted from the configuration file (
     * `/conf/application.conf`).
     *
     * Defines a set of handy helper methods to retrieve the application configuration values.
     */
    object configuration {

      val SIGN_VALIDATE_KEY = "application.signature.validate"
      val DATE_VALIDATE_THRESHOLD_SEC_KEY = "application.signature.threshold_sec"
      val DB_URI_KEY = "db_uri"
      val BOOTSTRAP_FILE = "application.bootstrap.file"

      def toMap: Map[String, String] = Map(
        SIGN_VALIDATE_KEY -> s"$shouldValidate",
        DATE_VALIDATE_THRESHOLD_SEC_KEY -> s"$validationThresholdSec",
        DB_URI_KEY -> dbUri,
        BOOTSTRAP_FILE -> s"$bootstrapFilepath"
      )


      private val shouldValidateSignature =
        Play.application.configuration.getBoolean(SIGN_VALIDATE_KEY)

      private val dateValidationThresholdSec =
        Play.application.configuration.getInt(DATE_VALIDATE_THRESHOLD_SEC_KEY)

      private val bootstrapFilepath =
        Play.application.configuration.getString(BOOTSTRAP_FILE)

      /**
       * Whether each request should be validated against the user's API Key.
       * Set by the `application.signature.validate` config value
       *
       * @return the boolean configuration value
       * @see [[SIGN_VALIDATE_KEY]]
       */
      def shouldValidate = shouldValidateSignature

      /**
       * When validating signed requests, the `Date:` header will be part of the signature and will
       * be validated against the server's current (UTC) time to be not older than
       * [[validationThresholdSec]] seconds.
       *
       * This is controlled by the `application.signature.threshold_sec` configuration value.
       *
       * @return the seconds that can be at most passed since this requests was signed
       * @see [[DATE_VALIDATE_THRESHOLD_SEC_KEY]]
       */
      def validationThresholdSec = dateValidationThresholdSec


      /**
       * The URI for the database connection, varies by type of DB, currently only
       * [[http://www.mongodb.org MongoDB]] supported; the URI is of the form:
       * {{{
       *   mongobd://host:port/db_name
       * }}}
       *
       * Use the `db_uri` configuration setting.
       *
       * @return the string representation of the URI; no validation is performed
       * @see [[com.alertavert.sentinel.persistence.DataAccessManager]]
       * @see [[DB_URI_KEY]]
       */
      def dbUri = Play.application.configuration.getString(DB_URI_KEY)

    /**
     * The `bootstrap file` to initialize the DB with one (or more) users who can then create
     * other users (via API calls)
     *
     * <p>The bootstrap file is a JSON file listing users (with their respective
     * usernames/passwords) that will be inserted into the DB at application startup: any
     * previously existing values will be updated by the values in the most recently starterd
     * servers.
     *
     * @return the full absolute path of the bootstrap file
     */
      def getBootstrapFilepath = bootstrapFilepath
  }
}

/**
 * This is the object used by the Play framework as the Application controller,
 * while we can use the Trait for unit testing.
 *
 * @see [[controllers.AppControllerSpec]]
 */
object AppController extends Controller with AppController
