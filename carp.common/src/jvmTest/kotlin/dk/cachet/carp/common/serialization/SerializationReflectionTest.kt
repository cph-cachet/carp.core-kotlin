package dk.cachet.carp.common.serialization

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.test.serialization.verifyTypesAreRegistered
import kotlin.test.*


class SerializationReflectionTest
{
    @Test
    fun all_AccountIdentity_types_registered_for_serialization() =
        verifyTypesAreRegistered<AccountIdentity>( COMMON_SERIAL_MODULE )

    @Test
    fun all_Data_types_registered_for_serialization() =
        verifyTypesAreRegistered<Data>( COMMON_SERIAL_MODULE )
}
