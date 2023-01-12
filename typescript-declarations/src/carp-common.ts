import * as common from "carp.core-kotlin-carp.common"


declare module "carp.core-kotlin-carp.common"
{
    // Base interfaces with better method names for internal types.
    namespace dk.cachet.carp.common.application.users
    {
        interface AccountIdentity {}
    }

    // Augment internal types to implement desired base interfaces.
    namespace dk.cachet.carp.common.application.users
    {
        interface EmailAccountIdentity extends AccountIdentity {}
        interface UsernameAccountIdentity extends AccountIdentity {}
    }
}

// Export facade.
export * from "carp.core-kotlin-carp.common"
