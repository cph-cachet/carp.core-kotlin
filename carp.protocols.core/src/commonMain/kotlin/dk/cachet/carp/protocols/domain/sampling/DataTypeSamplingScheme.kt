package dk.cachet.carp.protocols.domain.sampling

import dk.cachet.carp.common.EnumObjectList
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.tasks.measures.DataTypeMeasure


/**
 * Specifies the sampling scheme for a [DataType], including possible options, defaults, and constraints.
 */
abstract class DataTypeSamplingScheme<TSamplingConfigurationBuilder : SamplingConfigurationBuilder>(
    /**
     * The [DataType] this sampling scheme relates to.
     */
    val type: DataType
)
{
    /**
     * Create a [SamplingConfigurationBuilder] to help construct a matching [SamplingConfiguration] for [type].
     */
    protected abstract fun createSamplingConfigurationBuilder(): TSamplingConfigurationBuilder

    /**
     * Get the [DataTypeMeasure]
     */
    fun measure() = DataTypeMeasure( type )

    /**
     * Create a [SamplingConfiguration] which can be used to configure measures of [type].
     */
    fun samplingConfiguration( builder: TSamplingConfigurationBuilder.() -> Unit ): SamplingConfiguration =
        createSamplingConfigurationBuilder().apply( builder ).build()
}


/**
 * A helper class to construct iterable objects which hold [DataTypeSamplingScheme] member definitions.
 * This is similar to an enum, but removes the need for an intermediate enum type and generic type parameters are retained per member.
 *
 * Extend from this class as an object and assign members as follows: `val SCHEME = add( Scheme( options ) )`.
 */
abstract class DataTypeSamplingSchemeList : EnumObjectList<DataTypeSamplingScheme<*>>()
