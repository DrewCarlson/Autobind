package io.hypno.autobind

import com.squareup.kotlinpoet.CodeBlock
import javax.lang.model.element.Element


/**
 * Bindings are distinct for a Type when do not share a tag.
 * If a tag collision occurs, Bindings remain distinct if they
 * do not share a module.<br/>
 *
 * A later validation will determine (to the best of its ability)
 * if the Modules with type overlap will conflict.
 */
fun assertHasDistinctBindings(element: Element, providerKinds: List<ProviderKind<*>>) {
  val tagCollisionMap = providerKinds
      .groupingBy { it.tagForElement(element) }
      .eachCount()
      .filter { it.value > 1 }

  if (tagCollisionMap.none()) return

  val moduleCollisionMap = tagCollisionMap
      .map { it.key }
      .flatMap { tag ->
        providerKinds.filter { it.tagForElement(element) == tag }
      }
      .groupingBy { it.moduleClassForElement(element) }
      .eachCount()
      .filter { it.value > 1 }

  if (moduleCollisionMap.none()) return

  throw Exception("Error binding ${element.simpleName}: Cannot have duplicate type bindings without distinct tags or module.")
}

fun assertHasConstructor(element: Element) {
  if (element.constructors.isEmpty()) {
    throw Exception("Error binding ${element.simpleName}: Dependencies cannot be provided without a constructor.")
  }
}

fun assertHasNoArgConstructorForInstanceBinding(element: Element) {
  if (element.constructors.filter { it.parameterTypes.isEmpty() }.none()) {
    throw Exception("Error binding ${element.simpleName}: Instance bindings require a no-arg constructor or use a provider function.")
  }
}

fun assertHasFactoryCompatibleConstructor(element: Element) {
  if (element.factoryConstructors.none()) {
    throw Exception("Error binding ${element.simpleName}: Factory bindings require a 1-5 arg constructor or use a provider function.")
  }
}

fun assertHasMultitonCompatibleConstructor(element: Element) {
  if (element.factoryConstructors.none()) {
    throw Exception("Error binding ${element.simpleName}: Instance bindings require a 1-5 constructor or use a provider function.")
  }
}

/**
 *
 */
fun CodeBlock.Builder.assertHasConstructor(element: Element): CodeBlock.Builder {
  if (element.constructors.isEmpty()) {
    throw Exception("Error binding ${element.simpleName}: Dependencies cannot be provided without a constructor.")
  }
  return this
}