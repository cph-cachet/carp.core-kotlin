@file:Suppress( "FunctionName" )

package dk.cachet.carp.test.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerialModuleCollector
import kotlin.reflect.KClass
import kotlin.test.*


/**
 * A base test class to verify whether serialization for all supported types in a specified [serialModule] succeed.
 */
abstract class ConcreteTypesSerializationTest(
    /**
     * The JSON serializer to use.
     */
    private val json: Json,
    /**
     * The serial module containing the concrete types for which serialization needs to be tested.
     */
    private val serialModule: SerialModule,
    /**
     * For each of the types registered in [serialModule], an instance to be used to test serialization.
     */
    private val instancesToSerialize: List<Any>
)
{
    @Test
    fun all_types_in_serial_module_are_tested()
    {
        val typesToTest = getPolymorphicSerializers( serialModule ).keys
        for ( type in typesToTest )
        {
            val typeIsTested = instancesToSerialize.any { it::class == type }
            assertTrue( typeIsTested, "Missing instance of type '$type' to test serialization." )
        }
    }

    @Test
    fun can_serialize_all_instances_using_JSON()
    {
        for ( toSerialize in instancesToSerialize )
        {
            // Get serializer.
            val type = toSerialize::class
            val serializer = getPolymorphicSerializers( serialModule )[ type ] as KSerializer<Any>
            assertNotNull( serializer, "No serializer registered for type '$type'" )

            // Verify whether serializing and deserializing the instance results in the same object.
            val serialized = json.stringify( serializer, toSerialize )
            val parsed = json.parse( serializer, serialized )
            assertEquals( toSerialize, parsed, "Serialization of type '$type' failed." )
        }
    }


    private fun getPolymorphicSerializers( serialModule: SerialModule ): Map<KClass<*>, KSerializer<*>>
    {
        val collector =
            object : SerialModuleCollector
            {
                val serializers: MutableMap<KClass<*>, KSerializer<*>> = mutableMapOf()

                override fun <T : Any> contextual( kClass: KClass<T>, serializer: KSerializer<T> ) =
                    throw UnsupportedOperationException()

                override fun <Base : Any, Sub : Base> polymorphic( baseClass: KClass<Base>, actualClass: KClass<Sub>, actualSerializer: KSerializer<Sub> )
                {
                    serializers[ actualClass ] = actualSerializer
                }
            }

        serialModule.dumpTo( collector )
        return collector.serializers
    }
}
