import com.alertavert.sentinel.model.{Edit, Action}
object foo {
  var perms = scala.collection.mutable.Set[Action]()
  def baz() {
    perms += Edit.action
    println(perms)
  }

val x: Option[Int] = Some(5)

val st = x match {
  case None => ""
  case Some(n) => n.toString

}
println(st)

