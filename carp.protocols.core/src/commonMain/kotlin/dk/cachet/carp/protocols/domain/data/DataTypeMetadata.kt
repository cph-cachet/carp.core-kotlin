package dk.cachet.carp.protocols.domain.data


/**
 * Allows accessing metadata for a [DataType], including possible sampling configuration options.
 */
abstract class DataTypeMetadata<TSamplingConfigurationBuilder : SamplingConfigurationBuilder>(
    /**
     * The [DataType] this meta data relates to.
     */
    @Suppress( "ConstructorParameterNaming" )
    val TYPE: DataType
)
{
    /**
     * Create a [SamplingConfigurationBuilder] to help construct a matching [SamplingConfiguration] for [TYPE].
     */
    protected abstract fun createSamplingConfigurationBuilder(): TSamplingConfigurationBuilder

    /**
     * Create a [SamplingConfiguration] which can be used to configure measures of [TYPE].
     */
    fun samplingConfiguration( builder: TSamplingConfigurationBuilder.() -> Unit ): SamplingConfiguration =
        createSamplingConfigurationBuilder().apply( builder ).build()
}
