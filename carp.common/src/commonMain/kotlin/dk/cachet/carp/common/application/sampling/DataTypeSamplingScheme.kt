package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.EnumObjectMap
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.tasks.Measure


/**
 * Specifies the sampling scheme for a [DataType], including possible options, defaults, and constraints.
 */
abstract class DataTypeSamplingScheme<TConfigBuilder : SamplingConfigurationBuilder<*>>(
    /**
     * Information about the data type this sampling scheme relates to.
     */
    val dataType: DataTypeMetaData
)
{
    /**
     * Create a [SamplingConfigurationBuilder] to help construct a matching [SamplingConfiguration] for [dataType].
     */
    protected abstract fun createSamplingConfigurationBuilder(): TConfigBuilder

    /**
     * Create a [SamplingConfiguration] which can be used to configure measures of [dataType].
     *
     * @throws IllegalArgumentException when a sampling configuration is built which breaks constraints specified in this sampling scheme.
     */
    fun samplingConfiguration( builder: TConfigBuilder.() -> Unit ): SamplingConfiguration =
        createSamplingConfigurationBuilder().apply( builder ).build( this )

    /**
     * Create a [Measure] for the [dataType] defined by this sampling scheme,
     * and optionally override the device's default [SamplingConfiguration] for this [dataType].
     *
     * @throws IllegalArgumentException when a sampling configuration is built which breaks constraints specified in this sampling scheme.
     */
    fun measure( samplingConfigurationBuilder: (TConfigBuilder.() -> Unit)? = null ): Measure.DataStream =
        Measure.DataStream( dataType.type, samplingConfigurationBuilder?.let { samplingConfiguration( it ) } )

    /**
     * Determines whether [configuration] is valid for the constraints defined in this sampling scheme.
     */
    abstract fun isValid( configuration: SamplingConfiguration ): Boolean
}


/**
 * A helper class to construct iterable objects which hold [DataTypeSamplingScheme] member definitions.
 * This is similar to an enum, but removes the need for an intermediate enum type and generic type parameters are retained per member.
 *
 * Extend from this class as an object and assign members as follows: `val SCHEME = add( Scheme( options ) )`.
 */
open class DataTypeSamplingSchemeMap :
    EnumObjectMap<DataType, DataTypeSamplingScheme<*>>( { scheme -> scheme.dataType.type } )
