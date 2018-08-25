package io.hypno.autobind.builders

import com.squareup.kotlinpoet.CodeBlock
import io.hypno.autobind.BindingProvider
import io.hypno.autobind.ProviderKind


fun CodeBlock.Builder.addBindingProviders(bindingProviders: List<BindingProvider>): CodeBlock.Builder {
  return bindingProviders
      .map(::addBindingProvider)
      .last()
}

fun CodeBlock.Builder.addBindingProvider(bindingProvider: BindingProvider): CodeBlock.Builder {
  val (element, providerKinds) = bindingProvider
  return providerKinds
      .map { providerKind ->
        when (providerKind) {
          is ProviderKind.FACTORY -> addBindingForFactory(element)
          is ProviderKind.MULTITON -> addBindingForMultiton(element)
          is ProviderKind.INSTANCE -> addBindingForInstance(element)
          is ProviderKind.PROVIDER -> addBindingForProvider(element)
          is ProviderKind.SINGLETON -> addBindingForSingleton(element)
        }
      }
      .last()
}

fun CodeBlock.Builder.addBind(tag: String) =
    if (tag.isBlank()) add("bind()")
    else add("bind(tag = %S)", tag)