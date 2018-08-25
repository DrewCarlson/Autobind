package io.hypno.autobind.builders

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import io.hypno.autobind.*
import io.hypno.autobind.processing.kotlinConstructorData
import me.eugeniomarletti.kotlin.metadata.declaresDefaultValue
import javax.lang.model.element.Element


fun CodeBlock.Builder.addBindingForSingleton(element: Element): CodeBlock.Builder {
  val annotation = element.getAnnotation(Singleton::class.java)
  val tag = annotation.tag
  val ref = annotation.ref
  val provider = annotation.provider

  if (tag.isNotBlank() && ref.isNotBlank()) {
    beginControlFlow("bind(tag = %S) from %L(ref = %L)", tag, provider, ref)
  } else if (tag.isNotBlank()) {
    beginControlFlow("bind(tag = %S) from %L", tag, provider)
  } else if (ref.isNotBlank()) {
    beginControlFlow("bind() from %L(ref = %L)", provider, ref)
  } else {
    beginControlFlow("bind() from %L", provider)
  }

  val (_, kotlinMeta) = element.kotlinConstructorData.first()
  val params = kotlinMeta.valueParameterList

  val useNoArg = params.all { it.declaresDefaultValue }
  when {
    useNoArg -> add("%T()\n", element.asType().asTypeName())
    else -> {
      // TODO: handle named args or throw validation error
      add("%T(", element.asType().asTypeName())
      params
          .forEachIndexed { index, _ ->
            if (index > 0) add(", ")
            add("instance()")
          }
      add(")\n")
    }
  }

  endControlFlow()
  return this
}
