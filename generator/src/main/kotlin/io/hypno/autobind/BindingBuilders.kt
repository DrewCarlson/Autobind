package io.hypno.autobind

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Element

/* WARNING: Don't burn your eyes... I'll come back to this later. */

fun CodeBlock.Builder.addBindingProviders(bindingProviders: List<BindingProvider>): CodeBlock.Builder {
  bindingProviders
      .forEach {
        addBindingProvider(it)
      }
  return this
}

fun CodeBlock.Builder.addBindingProvider(bindingProvider: BindingProvider): CodeBlock.Builder {
  val (element, providerKinds) = bindingProvider
  providerKinds
      .forEach { providerKind ->
        when (providerKind) {
          is ProviderKind.FACTORY -> addBindingForFactory(element)
          is ProviderKind.MULTITON -> addBindingForMultiton(element)
          is ProviderKind.INSTANCE -> addBindingForInstance(element)
          is ProviderKind.PROVIDER -> addBindingForProvider(element)
          is ProviderKind.SINGLETON -> addBindingForSingleton(element)
        }
        add("\n")
      }
  return this
}

fun CodeBlock.Builder.addBindingForSingleton(element: Element): CodeBlock.Builder {
  val annotation = element.getAnnotation(Singleton::class.java)
  val constructor = element.constructors.first()
  // Add binding with provider and properties
  addBind(annotation.tag).add(" from ").add(when {
    annotation.eager -> "eagerSingleton"
    else -> "singleton"
  })

  add("(")
  when { // TODO: How can I make KotlinPoet Objects this properly?
    annotation.soft -> add("ref = softReference")
    annotation.weak -> add("ref = weakReference")
    annotation.threadLocal -> add("ref = threadLocal")
  }
  add(") {\n")

  indent()
  add("%T(", element.asType().asTypeName())
  constructor
      .parameterTypes
      .forEachIndexed { index, typeMirror ->
        // TODO: Inspect type, ensure we have a binding for it in the given module
        if (index > 0) add(", ")
        add("instance()")
      }
  add(")")
  unindent()
  add("\n}")
  return this
}

fun CodeBlock.Builder.addBindingForInstance(element: Element): CodeBlock.Builder {
  assertHasNoArgConstructorForInstanceBinding(element)
  val annotation = element.getAnnotation(Instance::class.java)
  // Add binding with provider and properties
  addBind(annotation.tag).add(" from instance(")
  add("%T()", element.asType().asTypeName())
  add(")")
  return this
}

fun CodeBlock.Builder.addBindingForFactory(element: Element): CodeBlock.Builder {
  assertHasFactoryCompatibleConstructor(element)
  val annotation = element.getAnnotation(Factory::class.java)
  val constructor = element.factoryConstructors.first()
  val params = constructor.parameterTypes
  addBind(annotation.tag).add(" from factory { ")
  params
      .forEachIndexed { i, typeMirror ->
        // TODO: Need a way to specify inject params vs factory params
        // TODO: Inspect type, ensure we have a binding for it in the given module
        if (i > 0) add(", ")
        // TODO: Use the constructor param names
        add("p$i: %T", typeMirror.asTypeName())
      }
  add(" -> \n")
  indent()
  add("%T(", element.asType().asTypeName())
  repeat(params.size) { i ->
    if (i > 0) add(", ")
    add("p$i")
  }
  add(")")
  unindent()
  add("\n}")
  return this
}

fun CodeBlock.Builder.addBindingForMultiton(element: Element): CodeBlock.Builder {
  assertHasFactoryCompatibleConstructor(element)
  val annotation = element.getAnnotation(Multiton::class.java)
  val constructor = element.factoryConstructors.first()
  val params = constructor.parameterTypes
  addBind(annotation.tag).add(" from multiton")

  add("(")
  when { // TODO: How can I make KotlinPoet Objects this properly?
    annotation.soft -> add("ref = softReference")
    annotation.weak -> add("ref = weakReference")
    annotation.threadLocal -> add("ref = threadLocal")
  }
  add(") { ")

  params
      .forEachIndexed { i, typeMirror ->
        // TODO: Need a way to specify inject params vs factory params
        // TODO: Inspect type, ensure we have a binding for it in the given module
        if (i > 0) add(", ")
        // TODO: Use the constructor param names
        add("p$i: %T", typeMirror.asTypeName())
      }
  add(" -> \n")
  indent()
  add("%T(", element.asType().asTypeName())
  repeat(params.size) { i ->
    if (i > 0) add(", ")
    add("p$i")
  }
  add(")")
  unindent()
  add("\n}")
  return this
}

fun CodeBlock.Builder.addBindingForProvider(element: Element): CodeBlock.Builder {
  val annotation = element.getAnnotation(Provider::class.java)
  val constructor = element.constructors.first()
  // Add binding with provider and properties
  addBind(annotation.tag).add(" from provider {\n")

  indent()
  add("%T(", element.asType().asTypeName())
  constructor
      .parameterTypes
      .forEachIndexed { index, typeMirror ->
        // TODO: Inspect type, ensure we have a binding for it in the given module
        if (index > 0) add(", ")
        add("instance()")
      }
  add(")")
  unindent()
  add("\n}")
  return this
}

fun CodeBlock.Builder.addBind(tag: String) =
    if (tag.isBlank()) add("bind()")
    else add("bind(tag = %S)", tag)