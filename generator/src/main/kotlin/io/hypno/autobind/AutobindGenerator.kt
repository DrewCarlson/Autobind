package io.hypno.autobind

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import org.kodein.di.Kodein
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


@AutoService(Processor::class)
class AutobindGenerator : KotlinAbstractProcessor(), KotlinMetadataUtils {

  private val procEnv: ProcessingEnvironment
    get() = (this as KotlinAbstractProcessor).processingEnv

  override fun getSupportedSourceVersion() = SourceVersion.latest()!!
  override fun getSupportedAnnotationTypes(): Set<String> =
      targetAnnotations.map { it.qualifiedName!! }.toSet()

  private val targetAnnotations = arrayOf(
      Module::class,
      Provider::class,
      Factory::class,
      Instance::class,
      Multiton::class,
      Singleton::class
  )

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    val providerElements = listOf<List<BindingProvider>>(
        roundEnv.getElementsWithAnnotation(Factory::class).map { it to listOf(ProviderKind.FACTORY) },
        roundEnv.getElementsWithAnnotation(Instance::class).map { it to listOf(ProviderKind.INSTANCE) },
        roundEnv.getElementsWithAnnotation(Multiton::class).map { it to listOf(ProviderKind.MULTITON) },
        roundEnv.getElementsWithAnnotation(Provider::class).map { it to listOf(ProviderKind.PROVIDER) },
        roundEnv.getElementsWithAnnotation(Singleton::class).map { it to listOf(ProviderKind.SINGLETON) }
    )

    val bindingProviders = getBindingProviders(providerElements)
    val defaultModuleProviders = getDefaultModuleProviders(bindingProviders)
    val customModuleBindingProviders = getCustomModuleBindingProviders(bindingProviders)
    val customModuleProviders = getCustomModuleProviders(customModuleBindingProviders)
    (defaultModuleProviders + customModuleProviders)
        .onEach(::generateModule)

    createDefaultModule()

    return true
  }

  fun getBindingProviders(providerElements: List<List<BindingProvider>>): List<BindingProvider> {
    return providerElements
        .flatMap { annotatedElements ->
          annotatedElements
              .map { (element) -> element }
        }
        .distinct()
        .map { element ->
          element to providerElements
              .filter { elementLists ->
                elementLists
                    .filter { (currentElement) -> currentElement == element }
                    .any()
              }
              .flatMap { elementLists ->
                elementLists
                    .filter { it.element == element }
                    .map(BindingProvider::providerKinds)
                    .fold(emptyList<ProviderKind<*>>()) { acc, next ->
                      acc + next
                    }
              }
        }
        .onEach { (element, providerKinds) ->
          assertHasDistinctBindings(element, providerKinds)
          assertHasConstructor(element)
        }
  }

  fun getDefaultModuleProviders(bindingProviders: List<BindingProvider>): List<ModuleProvider> {
    return bindingProviders
        .filter { (element, providerKinds) ->
          providerKinds
              .filter(element::needsDefaultTypeModule)
              .any()
        }
        .map { (element, providerKinds) ->
          element to providerKinds
              .filter(element::needsDefaultTypeModule)
        }
        .onEach {
          // TODO: Validate against ProviderKinds
          assertHasConstructor(it.element)
        }
        .map {
          it.element.asModuleClass() to listOf(it)
        }
  }

  fun getCustomModuleBindingProviders(bindingProviders: List<BindingProvider>): List<BindingProvider> {
    return bindingProviders
        // Drop kinds without custom modules
        .filter { (element, providerKinds) ->
          providerKinds
              .filterNot(element::needsDefaultTypeModule)
              .any()
        }
        // Remove type bindings without custom modules
        .map { (element, providerKinds) ->
          element to providerKinds
              .filterNot(element::needsDefaultTypeModule)
        }
  }

  fun getCustomModuleProviders(bindingProviders: List<BindingProvider>): List<ModuleProvider> {
    return bindingProviders
        .flatMap { (element, providerKinds) ->
          providerKinds
              .map(element::moduleClassForKind)
        }
        .distinct()
        .map { moduleClassName ->
          moduleClassName to bindingProviders
              .filter { (element, providerKinds) ->
                providerKinds
                    .filter { it.moduleClassForElement(element) == moduleClassName }
                    .any()
              }
              .map { (element, providerKinds) ->
                element to providerKinds
                    .filter { it.moduleClassForElement(element) == moduleClassName }
              }
        }
        .map { (moduleClassName, bindingProviders) ->
          moduleClassName.asModuleClass() to bindingProviders
        }
        .onEach(::logModuleProvider)
  }

  val defaultModuleProperties = mutableListOf<PropertySpec>()

  fun createDefaultModule() {
    val file = FileSpec.builder("io.hypno.autobind", "Autobind_DefaultModule")
        .addProperty(PropertySpec.builder("Autobind_DefaultModule", Kodein.Module::class)
            .initializer(CodeBlock.builder()
                .beginControlFlow("%T(%S)", Kodein.Module::class, "Autobind_DefaultModule")
                .apply {
                  defaultModuleProperties.forEach {
                    add("import(%N)\n", it)
                  }
                }
                .endControlFlow()
                .build())
            .build())
        .build()

    file.writeTo(generatedDir ?: throw IllegalStateException("Please use kapt."))
  }

  fun generateModule(moduleProvider: ModuleProvider) {
    val (moduleClass, bindingProviders) = moduleProvider
    val moduleName = "Autobind_${moduleClass.className}"

    val moduleDefCodeBlock = CodeBlock.builder()
        .beginControlFlow("%T(%S)", Kodein.Module::class, moduleName)
        .addBindingProviders(bindingProviders)
        .endControlFlow()
        .build()

    val modulePropertySpec = PropertySpec.builder(moduleName, Kodein.Module::class)
        .initializer(moduleDefCodeBlock)
        .build()

    val isDefaultModule = bindingProviders
        .take(1)
        .map { (element, providerKinds) ->
          providerKinds.first().needsDefaultTypeModule(element)
        }
        .single()

    if (isDefaultModule) {
      defaultModuleProperties.add(modulePropertySpec)
    }

    val file = FileSpec.builder(moduleClass.packageName, moduleName)
        // TODO: Improve import handling
        .addImport("org.kodein.di.generic", "bind", "singleton", "instance", "factory", "multiton", "provider")
        .addImport("org.kodein.di", "weakReference", "softReference", "threadLocal")
        .addProperty(modulePropertySpec)
        .build()

    file.writeTo(generatedDir ?: throw IllegalStateException("Please use kapt."))
  }

  private fun logModuleProvider(moduleProviders: ModuleProvider) {
    val (moduleClass, bindingProviders) = moduleProviders
    logDebug("")
    logDebug("--> Module: $moduleClass <--")
    bindingProviders.forEach { (element, providerKinds) ->
      logDebug("Type: $element - ProviderKinds: $providerKinds")
    }
    logDebug("")
  }

  private fun logDebug(message: String) =
      procEnv.messager.printMessage(Diagnostic.Kind.WARNING, message)

  private fun logError(message: String) =
      procEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)

  private val Element.packageName
    get() = procEnv.elementUtils.getPackageOf(this).toString()

  private fun Element.asModuleClass() =
      ModuleClass(packageName, simpleName.toString())
}