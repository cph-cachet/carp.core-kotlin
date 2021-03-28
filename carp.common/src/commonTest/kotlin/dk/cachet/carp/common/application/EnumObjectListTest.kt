package dk.cachet.carp.common.application

import kotlin.test.*


/**
 * Tests for [EnumObjectList].
 */
class EnumObjectListTest
{
    interface DataType { val typeName: String }

    object GeolocationType : DataType
    {
        override val typeName: String = "geolocation"

        fun create( longitude: Double, latitude: Double ) = Pair( longitude, latitude )
    }

    object SupportedTypes : EnumObjectList<DataType>()
    {
        val GEOLOCATION = add( GeolocationType )
    }


    @Test
    fun can_enumerate_and_access_interface_members()
    {
        val supportedTypes = SupportedTypes.map { it.typeName }
        assertEquals( listOf( GeolocationType.typeName ), supportedTypes )
    }

    @Test
    fun can_access_type_specific_members()
    {
        val geolocation = SupportedTypes.GEOLOCATION.create( 42.0, 42.0 )
        assertEquals( Pair( 42.0, 42.0 ), geolocation )
    }
}
