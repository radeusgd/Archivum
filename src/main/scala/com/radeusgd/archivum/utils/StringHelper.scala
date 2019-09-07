package com.radeusgd.archivum.utils

import java.text.Collator
import java.util.Locale

object StringHelper {
   val locale: Locale = new Locale("pl", "PL")
   val collator: Collator = Collator.getInstance(locale)
}
