import * as extend from "carp.core-kotlin-carp.common"


// Augment internal types to implement desired base interfaces.
declare module "carp.core-kotlin-carp.common"
{
    namespace dk.cachet.carp.common.application.users
    {
        interface AccountIdentity {}
        interface EmailAccountIdentity extends AccountIdentity {}
        interface UsernameAccountIdentity extends AccountIdentity {}
    }
}


// Export facade.
export * from "carp.core-kotlin-carp.common"
