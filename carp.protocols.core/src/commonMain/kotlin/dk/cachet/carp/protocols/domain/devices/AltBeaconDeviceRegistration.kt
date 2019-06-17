package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.*
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * A [DeviceRegistration] for [AltBeacon] specifying which beacon to listen to.
 *
 * The beacon ID is 20 bytes, made up out of the recommended subdivision [organizationId], [majorId], and [minorId].
 */
@Serializable
class AltBeaconDeviceRegistration(
    /**
     * The beacon device manufacturer's company identifier code as maintained by the Bluetooth SIG assigned numbers database.
     */
    var manufacturerId: Short,
    /**
     * The first 16 bytes of the beacon identifier which should be unique to the advertiser's organizational unit.
     */
    @Serializable( with = UUIDSerializer::class )
    var organizationId: UUID,
    /**
     * The first 2 bytes of the beacon identifier after the [organizationId], commonly named major ID.
     */
    var majorId: Short,
    /**
     * The last 2 bytes of the beacon identifier, commonly named minor ID.
     */
    var minorId: Short ) : DeviceRegistration()
{
    override var deviceId: String = "$manufacturerId"

    constructor() : this(
        0x0000,
        UUID( "00000000-0000-0000-0000-000000000000" ), 0x0000, 0x0000 )

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