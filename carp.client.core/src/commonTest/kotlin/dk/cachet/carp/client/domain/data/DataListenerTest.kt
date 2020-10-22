package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import kotlin.test.*


/**
 * Tests for [DataListener].
 */
class DataListenerTest
{
    private val unsupportedType = DataType( "not", "supported" )


    @Test
    fun supportsData_matches_supported_types_on_master_device()
    {
        val masterDataTypes = setOf( STUB_DATA_TYPE )
        val factory = StubDeviceDataCollectorFactory( masterDataTypes, connectedSupportedDataTypes = emptySet() )
        val listener = DataListener( factory )

        assertTrue( listener.supportsData( STUB_DATA_TYPE ) )
        assertFalse( listener.supportsData( unsupportedType ) )
    }

    @Test
    fun supportsDataOnConnectedDevice_matches_supported_types_on_connected_device()
    {
        val deviceType = StubDeviceDescriptor::class
        val factory = StubConnectedDeviceDataCollectorFactory(
            localSupportedDataTypes = emptySet(),
            connectedSupportedDataTypes = mapOf( deviceType to setOf( STUB_DATA_TYPE ) )
        )
        val listener = DataListener( factory )

        val registration = StubDeviceDescriptor().createRegistration()
        assertNotNull( listener.tryGetConnectedDataCollector( deviceType, registration ) )
        assertTrue( listener.supportsDataOnConnectedDevice( STUB_DATA_TYPE, deviceType, registration ) )
        assertFalse( listener.supportsDataOnConnectedDevice( unsupportedType, deviceType, registration ) )
    }

    @Test
    fun unsupported_connected_devices()
    {
        val factory = StubConnectedDeviceDataCollectorFactory( emptySet(), emptyMap() ) // Nothing is supported.
        val listener = DataListener( factory )

        val unsupportedDeviceType = StubDeviceDescriptor::class
        val registration = StubDeviceDescriptor().createRegistration()
        assertNull( listener.tryGetConnectedDataCollector( unsupportedDeviceType, registration ) )
        assertFalse( listener.supportsDataOnConnectedDevice( STUB_DATA_TYPE, unsupportedDeviceType, registration ) )
    }
}
