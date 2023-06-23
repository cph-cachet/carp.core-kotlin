import { expect } from 'chai'

import { kotlin } from '@cachet/carp-kotlin'
import Nullable = kotlin.Nullable
import mapOf = kotlin.collections.mapOf
import Map = kotlin.collections.Map
import Pair = kotlin.Pair

import { dk } from '@cachet/carp-deployments-core'

import common = dk.cachet.carp.common
import NamespacedId = dk.cachet.carp.common.application.NamespacedId
import Data = dk.cachet.carp.common.application.data.Data
import CarpInputDataTypes = dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import Sex = dk.cachet.carp.common.application.data.input.Sex
import UUID = common.application.UUID

import deployments = dk.cachet.carp.deployments
import ParticipationServiceRequest = deployments.infrastructure.ParticipationServiceRequest


describe( "carp-deployments-core", () => {
    describe( "ParticipationServiceRequest", () => {
        it( "can initialize SetParticipantData", () => {
            const inputByParticipantRole: string | null = null // Shorter alternative to using `Nullable<string>`.
            const participantData: Map<NamespacedId, Nullable<Data>> = // So, `Map<NamespacedId, Data | null>` would work as well.
                mapOf( [ new Pair( CarpInputDataTypes.SEX, Sex.Male ) ] )
            const setParticipantData = new ParticipationServiceRequest.SetParticipantData(
                UUID.Companion.randomUUID(),
                participantData,
                inputByParticipantRole
            )
        } )
    } )
} )
