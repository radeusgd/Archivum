package com.radeusgd.archivum.utils

import java.text.Collator
import java.util.Locale

object StringOrderHelper {
   val collator: Collator = Collator.getInstance(new Locale("pl", "PL"))
}
