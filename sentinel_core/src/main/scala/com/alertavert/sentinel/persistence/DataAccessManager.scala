// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.persistence

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.ValidBSONType.DBObject
import com.mongodb.casbah.{MongoDB, MongoConnection, MongoURI}
import com.alertavert.sentinel.persistence.mongodb.{MongoUserDao, ActionSerializer}
import com.alertavert.sentinel.errors.DbException

/**
 * Manage all accesses to a MongoDB via the underlying MongoClient.
 *
 * Prior to being used, this class needs to be initialized calling the ``init()`` method with a
 * valid URI (of the form ``mongodb://[host]:[port]/[db]``) and, once finished, it should be
 * closed by calling its ``close()`` method.
 *
 * <p>This DAM cannot be initialized multiple times (even with the same URI) and once closed
 * cannot be re-initialized or reused.
 *
 * <p>Ideally, ``init()`` should be called in the main program method, shortly after starting,
 * and then left alone until the program is about to terminate, when it can be safely closed.
 */
object DataAccessManager {

  private var conn: MongoConnection = _
  var db: MongoDB = _
  var closed = false

  /**
   * This must be called before any operation with the DAOs
   *
   * @param dbUri a URI that will point to the desired database
   */
  def init(dbUri: String) {
    if (conn != null) {
      throw new DbException("Data Manager already initialized; before re-initializing, " +
        "please close the connection, by calling DataAccessManager.close()")
    }
    if (closed) {
      throw new DbException("The DB connection has been closed and cannot be safely re-opened " +
        "during the lifetime of this program")
    }
    val mongoUri = MongoURI(dbUri)
    val dbName = mongoUri.database.getOrElse(throw new IllegalArgumentException("MongoDB URI must" +
      " specify a database name (use: mongodb://host[:port]/database"))
    conn = MongoConnection(mongoUri)
    db = conn.getDB(dbName)

    // Recreate indexes if they were dropped
    createIndexes()

    // TODO: I'm not sure this is the best place to register the hooks
    ActionSerializer register
  }

  def isReady: Boolean = conn != null

  def createIndexes(): Unit = {
    // TODO: factor out the creation of the indexes by reading a configuration file (JSON)
    db.getCollection(MongoUserDao.USER_COLLECTION).ensureIndex(MongoDBObject("credentials.username" -> 1),
      "ByUsername", true)
  }

  def close() {
    if (conn != null) {
      // TODO: log a warning message
      conn.close()
    }
    closed = true
  }
}
