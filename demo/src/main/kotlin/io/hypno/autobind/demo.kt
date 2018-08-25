package io.hypno.autobind

@Factory(tag = "factory")
@Instance(tag = "instance")
@Multiton(tag = "multiton", soft = true)
@Provider(tag = "provider")
@Singleton
class RandomGenerator(
    val min: Int = 0,
    val max: Int = Int.MAX_VALUE
)


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

@Module interface DataModule {

  // TODO: Provider functions
  //@Singleton
  //fun provideRestDataSource(): RestDataSource
}
