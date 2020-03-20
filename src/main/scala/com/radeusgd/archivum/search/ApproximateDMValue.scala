package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.{DMValue, Model}

/*
 This is a hack needed to make universal search working.
 Why?
 Currently the Model and View are completely separated.
 View handles how internal values are serialized and deserialized for the user,
 so a field of one type in the model can be displayed and edited by various controls depending on the context.
 This adds great flexibility to shaping the view.
 Unfortunately, the search facility has no access to the view, it is only based on the model, but it also needs some serialization capability.
 Thus we approximate what kind of control could be used for a given type to be able to handle it.
 The approximation should be 'good enough' in most cases.
 */
case class ApproximateDMValue(model: Model) {
   def aproximateFromString(path: List[String], value: String): DMValue = ???
}
