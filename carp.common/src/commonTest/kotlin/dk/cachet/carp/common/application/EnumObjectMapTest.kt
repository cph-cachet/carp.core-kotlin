package dk.cachet.carp.common.application

import kotlin.test.*


/**
 * Tests for [EnumObjectMap].
 */
class EnumObjectMapTest
{
    interface DataType { val typeName: String }

    object GeolocationType : DataType
    {
        override val typeName: String = "geolocation"

        fun create( longitude: Double, latitude: Double ) = Pair( longitude, latitude )
    }

    object SupportedTypes : EnumObjectMap<String, DataType>( { type -> type.typeName } )
    {
        val GEOLOCATION = add( GeolocationType )
    }


    @Test
    fun can_enumerate_and_access_interface_members()
    {
        // Keys can be enumerated.
        assertEquals( setOf( GeolocationType.typeName ), SupportedTypes.keys )

        // Maps can be merged.
        val additionalType = object : DataType { override val typeName: String = "additional-type" }
        val additionalTypes =
            object : EnumObjectMap<String, DataType>( { type -> type.typeName } )
            {
                val ADDITIONAL_TYPE = add( additionalType )
            }
        val mergedMap: Map<String, DataType> = SupportedTypes + additionalTypes
        assertEquals( setOf( GeolocationType.typeName, additionalType.typeName ), mergedMap.keys )
    }

    @Test
    fun can_access_type_specific_members()
    {
        val geolocation = SupportedTypes.GEOLOCATION.create( 42.0, 42.0 )
        assertEquals( Pair( 42.0, 42.0 ), geolocation )
    }

    @Test
    fun add_fails_when_key_is_already_present()
    {
        assertFailsWith<IllegalArgumentException>
        {
            object : EnumObjectMap<String, DataType>( { type -> type.typeName } )
            {
                val GEOLOCATION = add( GeolocationType )
                val WILL_FAIL = add( GeolocationType )
            }
        }
    }
}
