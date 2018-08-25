package io.hypno.autobind.builders

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import io.hypno.autobind.*
import io.hypno.autobind.processing.assertHasFactoryCompatibleConstructor
import io.hypno.autobind.processing.kotlinConstructorData
import io.hypno.autobind.processing.kotlinNameResolver
import javax.lang.model.element.Element


fun CodeBlock.Builder.addBindingForFactory(element: Element): CodeBlock.Builder {
  assertHasFactoryCompatibleConstructor(element)
  val annotation = element.asFactory()

  val tag = annotation.tag
  if (tag.isNotBlank()) {
    add("bind(tag = %S) from factory { ", tag)
  } else {
    add("bind() from factory { ")
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

  val (typeInfo, kotlinMeta) = constructors.first()
  val params = typeInfo.parameterTypes

  params.forEachIndexed { i, typeMirror ->
    if (i > 0) add(", ")
    val valueParam = kotlinMeta.getValueParameter(i)
    val paramName = nameResolver.getString(valueParam.name)
    add("%L: %T", paramName, typeMirror.asTypeName())
    if (i == params.lastIndex) add(" -> \n")
  }

  indent()
  add("%T(", element.asType().asTypeName())
  repeat(params.size) { i ->
    if (i > 0) add(", ")
    val valueParam = kotlinMeta.getValueParameter(i)
    val paramName = nameResolver.getString(valueParam.name)
    add(paramName)
  }
  add(")")
  unindent()
  add("\n}\n")
  return this
}