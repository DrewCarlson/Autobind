[![Release](https://jitpack.io/v/DrewCarlson/Autobind.svg)](https://jitpack.io/#DrewCarlson/Autobind)

# Autobind
Autobind adds Module and Dependency declaration code generation to [Kodein][1].

*NOTE: The current implementation is exceedingly naive and does not offer much customization for instantiation.*

## Why Use Autobind
Autobind allows you to skip the manual effort of defining your bindings and enables more agile dependency graph construction.
With Autobind you can enjoy the retrieval side niceties of Kodein with little (or no) binding declaration effort.

*Note: Dependency graph validation is not yet implemented, all current validation ensures well-formed generated code.*

## Autobind API

Autobind's API annotations map directly to [Kodein provider types](http://kodein.org/Kodein-DI/?5.2/core#declaring-dependencies) and work as expected without any additional configuration.

*Note: Scopes and multibinding are not supported*

Module Annotations:
* `@Module` *Available for consistency, does not yet offer any features.*

Binding Annotations:
* `@Instance`
* `@Singleton`
* `@Factory`
* `@Provider`
* `@Multiton`

Constructor Selection Annotation:
* `@Inject` *NOT IMPLEMENTED*

All binding annotations accept the optional `tag` which is passed directly to the binding.
For a full list of binding options, look at the [annotation declarations](api/src/main/kotlin/io/hypno/autobind/bindings.kt).

By default, all bindings will be available in the generated `Autobind_DefaultModule`.
For small projects (or simple dependency graphs), this gives you a single module to introduce all generated bindings into a Kodein instance.

If you need more flexibility when importing generated bindings, Autobind will generate a module for each type as `Autobind_ClassName`.
These type specific modules will contain all bindings of that type which do not define a module, duplicate type bindings for a single module MUST include a unique tag.
The `Autobind_DefaultModule` simply imports all of these type specific modules automatically.


## Constructors

`@Singleton` and `@Provider`:
1. no-arg constructor
2. Inject all parameters

`@Multiton` and `@Factory`:
1. no-arg constructor
2. forward all params

`@Instance` only works with a no-arg constructor.

## Example

See [demo.kt](demo/src/main/kotlin/io/hypno/autobind/demo.kt).

```kotlin
@Singleton(DataModule::class)
class RestDataSource
@Singleton(DataModule::class)
class DiskDataSource
@Singleton(DataModule::class)
class MemoryDataSource

@Factory(DataModule::class, tag = "TestFactory")
@Singleton(DataModule::class)
class Repository(
    val restDataSource: RestDataSource,
    val diskDataSource: DiskDataSource,
    val memoryDataSource: MemoryDataSource
)

@Module interface DataModule
```

```kotlin
val Autobind_DataModule = Kodein.Module("Autobind_DataModule") {
  bind(tag = "TestFactory") from factory { restDataSource: RestDataSource, diskDataSource: DiskDataSource, memoryDataSource: MemoryDataSource -> 
    Repository(restDataSource, diskDataSource, memoryDataSource)
  }
  bind() from singleton {
    Repository(instance(), instance(), instance())
  }
  bind() from singleton {
    DiskDataSource()
  }
  bind() from singleton {
    MemoryDataSource()
  }
  bind() from singleton {
    RestDataSource()
  }
}

```

## Download
Autobind is available via Jitpack.
```groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
Add the dependency:
```groovy
dependencies {
  implementation 'com.github.DrewCarlson.autobind:api:VERSION'
  kapt 'com.github.DrewCarlson.autobind:generator:VERSION'
}
```

## Todo

* Disambiguate injected params and default properties
* Support multiple factories with the same type ([documentation](http://kodein.org/Kodein-DI/?5.2/core#_bind_the_same_type_to_different_factories))
* Support constructor selection
* Validate dependency availability

## License

    Copyright 2018 Andrew Carlson

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.'


[1]: https://github.com/Kodein-Framework/Kodein-DI