import * as carpCommon from "carp.core-kotlin-carp.common"


export namespace dk.cachet.carp.common.application
{
    export const EmailAddress = carpCommon.dk.cachet.carp.common.application.EmailAddress
    export const NamespacedId = carpCommon.dk.cachet.carp.common.application.NamespacedId
    export const toTrilean = carpCommon.$crossModule$.toTrilean

    export class Trilean
    {
        private constructor() {}
        static readonly TRUE: Trilean = toTrilean( true )
        static readonly FALSE: Trilean = toTrilean( false )
        static readonly UNKNOWN: Trilean = carpCommon.$crossModule$.Trilean_UNKNOWN_getInstance()
    }
}

export namespace dk.cachet.carp.common.application.data.input
{
    export const CarpInputDataTypes = carpCommon.dk.cachet.carp.common.application.data.input.CarpInputDataTypes
    export const InputDataTypeList = carpCommon.dk.cachet.carp.common.application.data.input.InputDataTypeList
}

export namespace dk.cachet.carp.common.application.data.input.elements
{
    export const SelectOne = carpCommon.dk.cachet.carp.common.application.data.input.elements.SelectOne
    export const Text = carpCommon.dk.cachet.carp.common.application.data.input.elements.Text
}

export namespace dk.cachet.carp.common.application.users
{
    export const EmailAccountIdentity = carpCommon.dk.cachet.carp.common.application.users.EmailAccountIdentity
    export const ExpectedParticipantData = carpCommon.dk.cachet.carp.common.application.users.ExpectedParticipantData
    export const ExpectedParticipantDataSerializer = new carpCommon.$crossModule$.Companion_82().serializer()
    export type AssignedTo = carpCommon.$crossModule$.AssignedTo
    export namespace AssignedTo
    {
        export const All = new carpCommon.$crossModule$.All()
        export const Roles = carpCommon.$crossModule$.Roles
    }
    export type ParticipantAttribute = carpCommon.$crossModule$.ParticipantAttribute
    export namespace ParticipantAttribute
    {
        export const DefaultParticipantAttribute = carpCommon.$crossModule$.DefaultParticipantAttribute
        export const CustomParticipantAttribute = carpCommon.$crossModule$.CustomParticipantAttribute
    }
}

export namespace dk.cachet.carp.common.infrastructure.serialization
{
    export const createDefaultJSON = carpCommon.dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
}
