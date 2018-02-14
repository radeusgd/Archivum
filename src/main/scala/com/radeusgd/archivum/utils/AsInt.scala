package com.radeusgd.archivum.utils

object AsInt {
   def unapply(s: String): Option[Int] = util.Try(s.toInt).toOption
}
