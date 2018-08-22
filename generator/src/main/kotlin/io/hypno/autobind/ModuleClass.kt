package io.hypno.autobind

import com.squareup.kotlinpoet.ClassName


data class ModuleClass(
    val packageName: String,
    val className: String
) {
  override fun toString() = "$packageName.$className"
}

fun ClassName.asModuleClass() =
    ModuleClass(packageName, simpleName)