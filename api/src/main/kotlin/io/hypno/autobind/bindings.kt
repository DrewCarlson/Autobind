package io.hypno.autobind

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass


/**
 * Apply to class with 1-5 argument constructor(s)
 */
@Retention(SOURCE)
@Target(CLASS, FUNCTION)
annotation class Factory(
    /**
     * An optional module this provider should be bound in.
     */
    val module: KClass<*> = Unit::class,
    /**
     * An optional tag for the binding.  Use distinct tags
     * when binding the same type multiple times.
     */
    val tag: String = "",
    /**
     * When true, Autobind will attempt to exhaust all
     * constructor possibilities as unique providers.<br/>
     */
    val exhaust: Boolean = true
)

/**
 * Apply to class with no-arg constructor
 */
@Retention(SOURCE)
@Target(CLASS, FUNCTION)
annotation class Instance(
    /**
     * An optional module this provider should be bound in.
     */
    val module: KClass<*> = Unit::class,
    /**
     * An optional tag for the binding.  Use distinct tags
     * when binding the same type multiple times.
     */
    val tag: String = ""
)

/**
 * Apply to class with 1-5 argument constructor(s)
 */
@Retention(SOURCE)
@Target(CLASS, FUNCTION)
annotation class Multiton(
    /**
     * An optional module this provider should be bound in.
     */
    val module: KClass<*> = Unit::class,
    /**
     * An optional tag for the binding.  Use distinct tags
     * when binding the same type multiple times.
     */
    val tag: String = "",
    /**
     * When true, Autobind will attempt to exhaust all
     * constructor possibilities as unique providers.<br/>
     */
    val exhaust: Boolean = true,
    /**
     * If true, this provider will bind with [weakReference]
     * as its [ref].
     */
    val weak: Boolean = false,
    /**
     * If true, this provider will bind with [softReference]
     * as its [ref].
     */
    val soft: Boolean = false,
    /**
     * If true, this provider will bind with [threadLocal]
     * as its [ref].
     */
    val threadLocal: Boolean = false
)

/**
 * If the target has constructor params,
 * they will be injected from kodein.
 */
@Retention(SOURCE)
@Target(CLASS, FUNCTION)
annotation class Provider(
    /**
     * An optional module this provider should be bound in.
     */
    val module: KClass<*> = Unit::class,
    /**
     * An optional tag for the binding.  Use distinct tags
     * when binding the same type multiple times.
     */
    val tag: String = ""
)

/**
 * If the target has constructor params,
 * they will be injected from kodein.
 */
@Retention(SOURCE)
@Target(CLASS, FUNCTION)
annotation class Singleton(
    /**
     * An optional module this provider should be bound in.
     */
    val module: KClass<*> = Unit::class,
    /**
     * An optional tag for the binding.  Use distinct tags
     * when binding the same type multiple times.
     */
    val tag: String = "",
    /**
     * Indicate the provider should be eager.  If set to
     * true the [weak], [soft], and [threadLocal] options
     * will be ignored.
     */
    val eager: Boolean = false,
    /**
     * If true, this provider will bind with [weakReference]
     * as its [ref].
     */
    val weak: Boolean = false,
    /**
     * If true, this provider will bind with [softReference]
     * as its [ref].
     */
    val soft: Boolean = false,
    /**
     * If true, this provider will bind with [threadLocal]
     * as its [ref].
     */
    val threadLocal: Boolean = false
)