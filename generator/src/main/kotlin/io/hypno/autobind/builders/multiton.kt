package io.hypno.autobind.builders

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import io.hypno.autobind.*
import io.hypno.autobind.processing.*
import javax.lang.model.element.Element


fun CodeBlock.Builder.addBindingForMultiton(element: Element): CodeBlock.Builder {
  assertHasFactoryCompatibleConstructor(element)

  val annotation = element.asMultiton()
  val tag = annotation.tag
  val ref = annotation.ref

  if (tag.isNotBlank() && ref.isNotBlank()) {
    add("bind(tag = %S) from multiton(ref = %L) { ", tag, ref)
  } else if (tag.isNotBlank()) {
    add("bind(tag = %S) from multiton { ", tag)
  } else if (ref.isNotBlank()) {
    add("bind() from multiton(ref = %L) { ", ref)
  } else {
    add("bind() from multiton { ")
  }

  val nameResolver = element.kotlinNameResolver
  val constructors = element.kotlinConstructorData

  if (constructors.isEmpty()) {
    throw Exception("Error ${element.simpleName}: Multiton requires a constructor with 1-5 parameters.")
  }

  if (constructors.size > 1) {
    //TODO: Handle provider proxy or @Inject
    throw Exception("Error ${element.simpleName}: Too many constructors, cannot generate binding.")
  }

  val constructor = constructors.first()
  val params = constructor
      .typeInfo
      .parameterTypes

  params.forEachIndexed { i, typeMirror ->
    if (i > 0) add(", ")
    val valueParam = constructor.kotlinMeta.getValueParameter(i)
    val paramName = nameResolver.getString(valueParam.name)
    add("%L: %T", paramName, typeMirror.asTypeName())
    if (i == params.lastIndex) add(" -> \n")
  }

  indent()
  add("%T(", element.asType().asTypeName())
  repeat(params.size) { i ->
    if (i > 0) add(", ")
    val valueParam = constructor.kotlinMeta.getValueParameter(i)
    val paramName = nameResolver.getString(valueParam.name)
    add(paramName)
  }
  add(")")
  unindent()
  add("\n}\n")
  return this
}
