package com.radeusgd.archivum.datamodel

object LiftDMValue {
   implicit def lift(s: String): DMString = DMString(s)
   implicit def lift(x: Int): DMInteger = DMInteger(x)
}
