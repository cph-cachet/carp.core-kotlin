package dk.cachet.carp.common.test.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.UUIDFactory


/**
 * Creates a UUIDs counting up from 1.
 */
class TestUUIDFactory : UUIDFactory
{
    private var id = 1

    override fun randomUUID(): UUID
    {
        val idHex = id++.toString( 16 ).padStart( 12, '0' )
        return UUID( "00000000-0000-0000-0000-$idHex" )
    }
}
