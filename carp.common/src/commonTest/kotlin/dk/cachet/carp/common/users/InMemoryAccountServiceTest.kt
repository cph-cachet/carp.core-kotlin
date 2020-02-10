package dk.cachet.carp.common.users


/**
 * Tests for [InMemoryAccountService].
 */
class InMemoryAccountServiceTest : AccountServiceTest()
{
    override fun createService(): AccountService = InMemoryAccountService()
}
