package com.alertavert.sentinel.persistence

import org.bson.types.ObjectId
import java.util.Date

trait HasCreator {
  var createdBy: ObjectId = null
  var createdAt: Date = new Date
}
