package io.hypno.autobind

import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*


/**
 *
 */
@Retention(SOURCE)
@Target(CLASS)
annotation class Module(
    /**
     * All Kodein Modules should have a unique name.
     * By default Autobind will use `Autobind_ClassName`.
     * You are free to use anything your want but when
     * using custom names, it's on you to avoid duplication.
     */
    val name: String = ""
)
