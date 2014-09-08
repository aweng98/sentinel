
val s ="user:marco;hash:fferzfer=="

val segments = s.split(";")

val usr = segments.filter(_.startsWith("user")).head.split(":")(1)

List("a", "b").mkString("--", ", ", ";")
