package com.alertavert.sentinel.persistence

import com.mongodb.casbah.Imports.ObjectId

trait HasId {
  def id: Option[ObjectId]

  def setId(id: ObjectId): Unit
}
