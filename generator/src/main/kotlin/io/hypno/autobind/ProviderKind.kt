package io.hypno.autobind

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import io.hypno.autobind.processing.safeClassName
import javax.lang.model.element.Element
import kotlin.reflect.KClass

/**
 * Represents a Kodein provider and maps it to an Autobind annotation.
 */
sealed class ProviderKind<T : Annotation> {
  object FACTORY : ProviderKind<Factory>() {
    override val annotation = Factory::class
  }

  object INSTANCE : ProviderKind<Instance>() {
    override val annotation = Instance::class
  }

  object MULTITON : ProviderKind<Multiton>() {
    override val annotation = Multiton::class
  }

  object PROVIDER : ProviderKind<Provider>() {
    override val annotation = Provider::class
  }

  object SINGLETON : ProviderKind<Singleton>() {
    override val annotation = Singleton::class
  }

  abstract val annotation: KClass<out T>

  fun tagForElement(element: Element): String = when (this) {
    is ProviderKind.FACTORY -> element.getAnnotation(annotation.java).tag
    is ProviderKind.INSTANCE -> element.getAnnotation(annotation.java).tag
    is ProviderKind.MULTITON -> element.getAnnotation(annotation.java).tag
    is ProviderKind.PROVIDER -> element.getAnnotation(annotation.java).tag
    is ProviderKind.SINGLETON -> element.getAnnotation(annotation.java).tag
  }

  fun moduleClassForElement(element: Element): ClassName = when (this) {
    is ProviderKind.FACTORY -> element.getAnnotation(annotation.java).safeClassName { module }
    is ProviderKind.INSTANCE -> element.getAnnotation(annotation.java).safeClassName { module }
    is ProviderKind.MULTITON -> element.getAnnotation(annotation.java).safeClassName { module }
    is ProviderKind.PROVIDER -> element.getAnnotation(annotation.java).safeClassName { module }
    is ProviderKind.SINGLETON -> element.getAnnotation(annotation.java).safeClassName { module }
  }

  fun isForModule(element: Element, moduleClass: KClass<*>) =
      moduleClassForElement(element) == moduleClass.asTypeName()

  fun needsDefaultTypeModule(element: Element) =
      isForModule(element, Unit::class)
}

fun Element.needsDefaultTypeModule(providerKind: ProviderKind<*>) =
    providerKind.needsDefaultTypeModule(this)

fun Element.moduleClassForKind(providerKind: ProviderKind<*>) =
    providerKind.moduleClassForElement(this)