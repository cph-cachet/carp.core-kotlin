import VerifyModule from './VerifyModule'
import { expect } from 'chai'

import { kotlin } from '../src/kotlin'
import setOf = kotlin.collections.setOf
import listOf = kotlin.collections.listOf

import { kotlinx as kcd } from '../src/kotlinx-datetime'
import Clock = kcd.datetime.Clock

import { dk as cdk } from 'carp.core-kotlin-carp.common'
import UUID = cdk.cachet.carp.common.application.UUID
import AssignedTo = cdk.cachet.carp.common.application.users.AssignedTo

import { dk } from 'carp.core-kotlin-carp.studies.core'
import StudyStatus = dk.cachet.carp.studies.application.StudyStatus
import AssignedParticipantRoles = dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import participantIds = dk.cachet.carp.studies.application.users.participantIds
import participantRoles = dk.cachet.carp.studies.application.users.participantRoles


describe( "carp.studies.core", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = []

        const moduleVerifier = new VerifyModule(
            'carp.core-kotlin-carp.studies.core',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "AssignedParticipantRoles", () => {
        it( "getAssigned participantIds and participantRoles works", () => {
            const participant1 = UUID.Companion.randomUUID()
            const participant2 = UUID.Companion.randomUUID()
            const assigned1 = new AssignedParticipantRoles( participant1, new AssignedTo.Roles( setOf( [ "Test" ] ) ) )
            const assigned2 = new AssignedParticipantRoles( participant2, AssignedTo.All )
            const assignedGroup = listOf( [ assigned1, assigned2 ] )
            expect( participantIds( assignedGroup ).contains( participant1 ) ).is.true
            expect( participantRoles( assignedGroup ).contains( "Test" ) ).is.true
        } )
    } )

    
    describe( "StudyStatus", () => {
        it ( "can typecheck StudyStatus", () => {
            const configuring = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", Clock.System.now(), null, true, true, false, true )
            const configuringStatus: StudyStatus = configuring
            expect( configuringStatus instanceof StudyStatus.Configuring ).is.true
            expect( configuringStatus instanceof StudyStatus.Live ).is.false

            const live = new StudyStatus.Live( UUID.Companion.randomUUID(), "Test", Clock.System.now(), UUID.Companion.randomUUID(), false, false, true )
            const liveStatus: StudyStatus = live
            expect( liveStatus instanceof StudyStatus.Live ).is.true
            expect( liveStatus instanceof StudyStatus.Configuring ).is.false
        } )
    } )
} )
