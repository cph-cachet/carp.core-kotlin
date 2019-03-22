package dk.cachet.carp.common.serialization


/**
 * Provides access to serialized JSON ([jsonSource]) of an instance of the class identified by the fully qualified [className].
 * This interface needs to be implemented by wrapper objects returned by [UnknownPolymorphicSerializer].
 */
interface UnknownPolymorphicWrapper
{
    val className: String
    val jsonSource: String
}