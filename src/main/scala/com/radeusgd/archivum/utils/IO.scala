package com.radeusgd.archivum.utils

object IO {

   def readFile(relativePath: String): String = {
      val f = io.Source.fromFile(relativePath)
      try {
         f.getLines.mkString("\n")
      } finally {
         f.close()
      }
   }

}