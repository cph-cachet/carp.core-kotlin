package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.*
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * A beacon meeting the open AltBeacon standard.
 */
@Serializable
data class AltBeacon( override val roleName: String ) : DeviceDescriptor<AltBeaconDeviceRegistrationBuilder>()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                AltBeacon::class,
                AltBeacon.serializer(),
                "dk.cachet.carp.protocols.domain.devices.AltBeacon" )
        }
    }

    override fun createDeviceRegistrationBuilder(): AltBeaconDeviceRegistrationBuilder = AltBeaconDeviceRegistrationBuilder()
    override fun isValidConfiguration( registration: DeviceRegistration ): Trilean
        = ( registration is AltBeaconDeviceRegistration ).toTrilean()
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
    @Serializable( with = UUIDSerializer::class )
    val organizationId: UUID,
    /**
     * The first 2 bytes of the beacon identifier after the [organizationId], commonly named major ID.
     */
    val majorId: Short,
    /**
     * The last 2 bytes of the beacon identifier, commonly named minor ID.
     */
    val minorId: Short ) : DeviceRegistration()
{
    override val deviceId: String = "$manufacturerId:$organizationId:$majorId:$minorId"

    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                AltBeaconDeviceRegistration::class,
                AltBeaconDeviceRegistration.serializer(),
                "dk.cachet.carp.protocols.domain.devices.AltBeaconDeviceRegistration" )
        }
    }
}


@Serializable
class AltBeaconDeviceRegistrationBuilder : DeviceRegistrationBuilder()
{
    private var manufacturerId: Short = 0x0000
    /**
     * Set the beacon's device manufacturer's company identifier code as maintained by the Bluetooth SIG assigned numbers database.
     */
    fun manufacturerId( getId: () -> Short ) { this.manufacturerId = getId() }

    @Serializable( with = UUIDSerializer::class )
    private var organizationId: UUID = UUID( "00000000-0000-0000-0000-000000000000" )
    /**
     * Set the first 16 bytes of the beacon identifier which should be unique to the advertiser's organizational unit.
     */
    fun organizationId( getId: () -> UUID ) { this.organizationId = getId() }

    private var majorId: Short = 0x0000
    /**
     * Set the first 2 bytes of the beacon identifier after the [organizationId], commonly named major ID.
     */
    fun majorId( getId: () -> Short ) { this.majorId = getId() }

    private var minorId: Short = 0x0000
    /**
     * Set the last 2 bytes of the beacon identifier, commonly named minor ID.
     */
    fun minorId( getId: () -> Short ) { this.minorId = getId() }

    override fun build(): AltBeaconDeviceRegistration
        = AltBeaconDeviceRegistration( manufacturerId, organizationId, majorId, minorId )
}