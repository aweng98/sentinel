package com.alertavert.sentinel.persistence

import java.util.Date
import com.alertavert.sentinel.model.User

trait HasCreator {
  var createdBy: Option[User] = null
  var createdAt: Date = new Date
}
