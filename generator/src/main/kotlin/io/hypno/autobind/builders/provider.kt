package io.hypno.autobind.builders

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import io.hypno.autobind.*
import io.hypno.autobind.processing.kotlinConstructorData
import me.eugeniomarletti.kotlin.metadata.declaresDefaultValue
import javax.lang.model.element.Element


fun CodeBlock.Builder.addBindingForProvider(element: Element): CodeBlock.Builder {
  val tag = element.asProvider().tag
  if (tag.isNotBlank()) {
    beginControlFlow("bind(tag = %S) from provider", tag)
  } else {
    beginControlFlow("bind() from provider")
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