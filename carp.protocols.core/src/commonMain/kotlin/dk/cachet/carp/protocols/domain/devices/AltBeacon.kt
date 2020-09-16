package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.serialization.NotSerializable
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A beacon meeting the open AltBeacon standard.
 */
@Serializable
data class AltBeacon( override val roleName: String ) : DeviceDescriptor<AltBeaconDeviceRegistration, AltBeaconDeviceRegistrationBuilder>()
{
    // The AltBeacon protocol does not expose any measures. Other devices measure proximity to the beacon.
    // TODO: Some beacons do include information such as battery charge and temperature.
    override val supportedDataTypes: Set<DataType> = emptySet()

    override val samplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): AltBeaconDeviceRegistrationBuilder = AltBeaconDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<AltBeaconDeviceRegistration> = AltBeaconDeviceRegistration::class
    override fun isValidConfiguration( registration: AltBeaconDeviceRegistration ): Trilean = Trilean.TRUE
}


/**
 * A [DeviceRegistration] for [AltBeacon] specifying which beacon to listen to.
 *
 * The beacon ID is 20 bytes, made up out of the recommended subdivision [organizationId], [majorId], and [minorId].
 */
@Serializable
data class AltBeaconDeviceRegistration(
    /**
     * The beacon device manufacturer's company identifier code as maintained by the Bluetooth SIG assigned numbers database.
     */
    val manufacturerId: Short,
    /**
     * The first 16 bytes of the beacon identifier which should be unique to the advertiser's organizational unit.
     */
    val organizationId: UUID,
    /**
     * The first 2 bytes of the beacon identifier after the [organizationId], commonly named major ID.
     */
    val majorId: Short,
    /**
     * The last 2 bytes of the beacon identifier, commonly named minor ID.
     */
    val minorId: Short
) : DeviceRegistration()
{
    override val deviceId: String =
        // TODO: Remove this workaround once JS serialization bug is fixed: https://github.com/Kotlin/kotlinx.serialization/issues/716
        if ( arrayOf( manufacturerId, organizationId, majorId, minorId ).any { it == null } ) ""
        else "$manufacturerId:$organizationId:$majorId:$minorId"
}


@Serializable( with = NotSerializable::class )
class AltBeaconDeviceRegistrationBuilder : DeviceRegistrationBuilder<AltBeaconDeviceRegistration>
{
    /**
     * The beacon's device manufacturer's company identifier code as maintained by the Bluetooth SIG assigned numbers database.
     */
    var manufacturerId: Short = 0x0000

    /**
     * The first 16 bytes of the beacon identifier which should be unique to the advertiser's organizational unit.
     */
    var organizationId: UUID = UUID( "00000000-0000-0000-0000-000000000000" )

    /**
     * The first 2 bytes of the beacon identifier after the [organizationId], commonly named major ID.
     */
    var majorId: Short = 0x0000

    /**
     * The last 2 bytes of the beacon identifier, commonly named minor ID.
     */
    var minorId: Short = 0x0000

    override fun build(): AltBeaconDeviceRegistration =
        AltBeaconDeviceRegistration( manufacturerId, organizationId, majorId, minorId )
}
