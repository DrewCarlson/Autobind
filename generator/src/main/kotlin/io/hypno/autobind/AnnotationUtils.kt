package io.hypno.autobind

import javax.lang.model.element.Element

fun Element.asInstance() = getAnnotation(Instance::class.java)
fun Element.asProvider() = getAnnotation(Provider::class.java)
fun Element.asFactory() = getAnnotation(Factory::class.java)
fun Element.asMultiton() = getAnnotation(Multiton::class.java)
fun Element.asSingleton() = getAnnotation(Singleton::class.java)

val Multiton.ref
  get() = when {
    soft -> "softReference"
    weak -> "weakReference"
    threadLocal -> "threadLocal"
    else -> ""
  }

val Singleton.ref
  get() = when {
    soft -> "softReference"
    weak -> "weakReference"
    threadLocal -> "threadLocal"
    else -> ""
  }

val Singleton.provider
  get() = when {
    eager -> "eagerSingleton"
    else -> "singleton"
  }