import com.alertavert.sentinel.model.{Edit, Action}
object foo {
  var perms = scala.collection.mutable.Set[Action]()
  def baz() {
    perms += Edit.action
    println(perms)
  }
}

val x: Option[Int] = Some(5)

val st = x match {
  case None => ""
  case Some(n) => n.toString
}

println(st)


val y: Option[Int] = Some(33)

def compOptions(x: Option[Int], y: Option[Int]) = {
  if ((x == None) && (y == None)) true
  else x.map(n => y.exists(_ == n)) getOrElse false
}
compOptions(None, None)
compOptions(None, Some(2))
compOptions(Some(4), Some(4))
compOptions(Some(5), None)

