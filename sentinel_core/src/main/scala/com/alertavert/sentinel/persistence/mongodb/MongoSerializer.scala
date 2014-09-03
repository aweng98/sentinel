package com.alertavert.sentinel.persistence.mongodb

import com.alertavert.sentinel.persistence.HasId
import com.mongodb.casbah.commons.MongoDBObject

trait MongoSerializer[T] {

  /**
   * This takes an object in the model domain and transforms into a Mongo-compatible
   * object that can be stored into the DB
   *
   * @param obj the object to serialize
   * @return a Mongo-compatible representation of the object
   */
  def serialize(obj: T): MongoDBObject

  /**
   * This operation is the reverse of the ``serialize`` one and converts a Mongo document into
   * a representation of the domain model object.
   *
   * @param item the document retrieved from the Mongo collection
   * @return the corresponding domain object
   */
  def deserialize(item: MongoDBObject): T
}
