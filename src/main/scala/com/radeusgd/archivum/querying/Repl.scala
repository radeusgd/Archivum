package com.radeusgd.archivum.querying

import scala.reflect.ClassTag
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.Settings
import scala.reflect.runtime.{universe => ru}

// work in progress

object Helper {
   var diff: Int = 5
   val f: Int => Int = (x: Int) => x + diff
}

class Repl(out: Option[java.io.PrintWriter] = None) {
   val eval: IMain = {
      val settings = new Settings
      settings.usejavacp.value = true
      settings.deprecation.value = true
      out match {
         case Some(pw) => new IMain(settings, pw)
         case None => new IMain(settings)
      }
   }

   def bind[T: ru.TypeTag : ClassTag](name:String, value: T): Unit = {
      eval.directBind[T](name, value)
   }

   def evaluate(line: String, silent: Boolean = false): Any = {
      val evaluated = if (silent) eval.beQuietDuring(eval.interpret(line)) else eval.interpret(line)
      null // TODO return result
   }


}