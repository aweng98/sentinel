// Copyright AlertAvert.com (c) 2014. All rights reserved.
// Commercial use or modification of this software without a valid license is expressly forbidden

package com.alertavert.sentinel.persistence.mongodb

import com.mongodb.casbah.commons.conversions.MongoConversionHelper
import org.bson.{BSON, Transformer}
import com.alertavert.sentinel.security.Action

/**
 * Serializes an Action using its name, and deserializes it back.
 */
object ActionSerializer extends MongoConversionHelper {

  private val transformer = new Transformer {
    def transform(o: AnyRef): AnyRef = o.toString
  }

  // TODO: use reflection to navigate the Action's subclasses and dynamically instantiate
  private val parser = new Transformer {
    def transform(o: AnyRef): AnyRef = o match {
      case name: String => if (Action.actionsMap.contains(name)) Action.actionsMap(name) else null
      Action.actionsMap(o.toString)
    }
  }

  override def register() {
    BSON.addEncodingHook(classOf[Action], transformer)
    BSON.addDecodingHook(classOf[Action], parser)
    super.register()
  }
}
