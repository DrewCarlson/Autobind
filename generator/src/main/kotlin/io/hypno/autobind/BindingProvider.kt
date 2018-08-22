package io.hypno.autobind

import javax.lang.model.element.Element

/**
 * A representation of all the data required to bind a type to a provider in a module.
 */
typealias BindingProvider = Pair<Element, List<ProviderKind<*>>>

/**
 * An [Element] used to explain the Type in a binding.
 */
val BindingProvider.element get() = first

/**
 * The ProviderKinds to bind.
 */
val BindingProvider.providerKinds get() = second

/**
 *
 */
typealias ModuleProvider = Pair<ModuleClass, List<BindingProvider>>

/**
 *
 */
val ModuleProvider.moduleClass get() = first

/**
 *
 */
val ModuleProvider.bindingProviders get() = second