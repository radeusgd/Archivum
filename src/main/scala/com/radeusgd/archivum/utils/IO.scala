package com.radeusgd.archivum.utils

import java.io.File

object IO {

   def resolveRelativePath(relativePath: String): File = new File(relativePath)

   def readFileString(file: File): String = {
      val f = io.Source.fromFile(file)
      try {
         f.getLines.mkString("\n")
      } finally {
         f.close()
      }
   }
   def readFileString(relativePath: String): String = readFileString(resolveRelativePath(relativePath))
}