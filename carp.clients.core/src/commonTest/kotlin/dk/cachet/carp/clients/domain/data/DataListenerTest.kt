package dk.cachet.carp.clients.domain.data

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import kotlin.test.*


/**
 * Tests for [DataListener].
 */
class DataListenerTest
{
    private val unsupportedType = DataType( "not", "supported" )


    @Test
    fun supportsData_matches_supported_types_on_primary_device()
    {
        val primaryDataTypes = setOf( STUB_DATA_POINT_TYPE )
        val factory = StubDeviceDataCollectorFactory( primaryDataTypes, connectedSupportedDataTypes = emptySet() )
        val listener = DataListener( factory )

        assertTrue( listener.supportsData( STUB_DATA_POINT_TYPE ) )
        assertFalse( listener.supportsData( unsupportedType ) )
    }

    @Test
    fun supportsDataOnConnectedDevice_matches_supported_types_on_connected_device()
    {
        val deviceType = StubDeviceConfiguration::class
        val factory = StubConnectedDeviceDataCollectorFactory(
            localSupportedDataTypes = emptySet(),
            connectedSupportedDataTypes = mapOf( deviceType to setOf( STUB_DATA_POINT_TYPE ) )
        )
        val listener = DataListener( factory )

        val registration = StubDeviceConfiguration().createRegistration()
        assertNotNull( listener.tryGetConnectedDataCollector( deviceType, registration ) )
        assertTrue( listener.supportsDataOnConnectedDevice( STUB_DATA_POINT_TYPE, deviceType, registration ) )
        assertFalse( listener.supportsDataOnConnectedDevice( unsupportedType, deviceType, registration ) )
    }

    @Test
    fun unsupported_connected_devices()
    {
        val factory = StubConnectedDeviceDataCollectorFactory( emptySet(), emptyMap() ) // Nothing is supported.
        val unsupportedDeviceType = StubDeviceConfiguration::class
        val registration = StubDeviceConfiguration().createRegistration()

        DataListener( factory ).apply {
            assertNull( tryGetConnectedDataCollector( unsupportedDeviceType, registration ) )
            assertFalse( supportsDataOnConnectedDevice( STUB_DATA_POINT_TYPE, unsupportedDeviceType, registration ) )
        }
    }
}
