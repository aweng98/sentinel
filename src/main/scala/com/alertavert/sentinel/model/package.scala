package com.alertavert.sentinel

import com.mongodb.casbah.Imports.ObjectId

/**
 * Created by marco on 3/6/14.
 */
package object model {

  def idEquals(x: Option[ObjectId], y: Option[ObjectId]) = {
    if ((x == None) && (y == None)) true
    else x.exists(n => y.exists(_ == n))
  }

}
