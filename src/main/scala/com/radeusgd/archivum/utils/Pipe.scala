package com.radeusgd.archivum.utils

object Pipe {
   implicit class Pipe[A, B](val f: A => B) extends AnyVal {
      def |>[C](g: B => C): A => C = a => g(f(a))
   }
}
