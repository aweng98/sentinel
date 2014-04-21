package com.alertavert.sentinel.persistence

import com.mongodb.casbah.Imports.ObjectId

trait HasId {
  var id: Option[ObjectId] = None

  def setId(id: ObjectId) = id match {
    case x: ObjectId => this.id = Some(id)
    case _ => this.id = None
  }
}
