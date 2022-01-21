package dk.cachet.carp.rpc

import kotlin.test.*


internal val applicationServices = ApplicationServiceInfo.findInNamespace( "dk.cachet.carp" )

class ApplicationServiceInfoTest
{
    @Test
    fun findInNamespace_succeeds()
    {
        assertFalse( applicationServices.isEmpty() )
    }
}
