package io.hypno.autobind.builders

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import io.hypno.autobind.asSingleton
import io.hypno.autobind.processing.assertHasNoArgConstructorForInstanceBinding
import javax.lang.model.element.Element


fun CodeBlock.Builder.addBindingForInstance(element: Element): CodeBlock.Builder {
  assertHasNoArgConstructorForInstanceBinding(element)
  val tag = element.asSingleton().tag
  return when {
    tag.isBlank() ->
      add("bind() from instance(%T())\n", element.asType().asTypeName())
    else ->
      add("bind(tag = %S) from instance(%T())\n", tag, element.asType().asTypeName())
  }
}
