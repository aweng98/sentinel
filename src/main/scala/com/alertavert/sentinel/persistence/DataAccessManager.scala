package com.alertavert.sentinel.persistence

import com.mongodb.casbah.{MongoDB, MongoConnection, MongoURI}
import com.alertavert.sentinel.persistence.mongodb.ActionSerializer

object DataAccessManager {

  // TODO: implement a more generic way of obtaining DAOs, also independent of the implementation
  var conn: MongoConnection = _
  var db: MongoDB = _

  /**
   * This must be called before any operation with the DAOs
   *
   * @param dbUri a URI that will point to the desired database
   */
  def init(dbUri: String) {
    val mongoUri = MongoURI(dbUri)
    val dbName = mongoUri.database.getOrElse(throw new IllegalArgumentException("MongoDB URI must" +
      " specify a database name (use: mongodb://host[:port]/database"))
    conn = MongoConnection(mongoUri)
    db = conn.getDB(dbName)

    // TODO: I'm not sure this is the best place to register the hooks
    ActionSerializer register
  }

  def isReady: Boolean = conn != null
}
