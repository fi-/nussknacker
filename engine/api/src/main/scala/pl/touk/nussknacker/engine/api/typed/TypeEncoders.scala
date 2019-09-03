package pl.touk.nussknacker.engine.api.typed

import argonaut.Argonaut._
import argonaut._
import pl.touk.nussknacker.engine.api.typed.typing._

object TypeEncoders {

  private def encodeTypedClass(ref: TypedClass): Json = jObjectFields(
    "refClazzName" -> jString(ref.klass.getName),
    "params" -> jArray(ref.params.map(encodeTypingResult))
  )

  private def encodeTypingResult(result: TypingResult): Json = result match {
    case typing.Unknown => encodeTypedClass(TypedClass(classOf[Any], List()))
    // FIXME: handle union
    case Typed(classes) => val headClass = classes.head
      encodeTypingResult(headClass)
    case TypedObjectTypingResult(fields, objType) => jObjectAssocList(
      //TODO: check if after objType was added still happens: map methods are suggested but validation fails?
      encodeTypedClass(objType).objectOrEmpty.toList
        :+ "fields" -> jObjectAssocList(fields.mapValues(encodeTypingResult).toList)
    )
    case cl: TypedClass => encodeTypedClass(cl)
  }

  implicit val clazzRefEncoder: EncodeJson[ClazzRef] = EncodeJson[ClazzRef](tc => encodeTypedClass(TypedClass(tc)))

  implicit val typingResultEncoder: EncodeJson[TypingResult] = EncodeJson[TypingResult](encodeTypingResult)

}

