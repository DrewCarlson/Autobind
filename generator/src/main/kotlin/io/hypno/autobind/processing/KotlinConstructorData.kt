package io.hypno.autobind.processing

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.lang.model.element.Element
import javax.lang.model.type.ExecutableType


typealias KotlinConstructorData = Pair<ExecutableType, ProtoBuf.Constructor>
val KotlinConstructorData.typeInfo get() = first
val KotlinConstructorData.kotlinMeta get() = second

val Element.kotlinConstructorData: List<KotlinConstructorData>
  get() {
    val metadata = kotlinMetadata as KotlinClassMetadata
    val (_, classProto) = metadata.data
    return constructors.zip(classProto.constructorList)
  }

val Element.kotlinNameResolver: NameResolver
  get() {
    val metadata = kotlinMetadata as KotlinClassMetadata
    val (nameResolver, _) = metadata.data
    return nameResolver
  }