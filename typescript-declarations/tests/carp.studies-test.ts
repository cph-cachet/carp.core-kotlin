import VerifyModule from './VerifyModule'
import { expect } from 'chai'

import { kotlin } from '../src/kotlin'
import setOf = kotlin.collections.setOf
import listOf = kotlin.collections.listOf

import { kotlinx as kcd } from '../src/kotlinx-datetime'
import Clock = kcd.datetime.Clock

import { kotlinx as scd } from '../src/kotlinx-serialization'
import ListSerializer = scd.serialization.builtins.ListSerializer

import { dk as cdk } from 'carp.core-kotlin-carp.common'
import UUID = cdk.cachet.carp.common.application.UUID
import Username = cdk.cachet.carp.common.application.users.Username
import AssignedTo = cdk.cachet.carp.common.application.users.AssignedTo
import UsernameAccountIdentity = cdk.cachet.carp.common.application.users.UsernameAccountIdentity
import JSON = cdk.cachet.carp.common.infrastructure.serialization.JSON

import { dk as ddk } from 'carp.core-kotlin-carp.deployments.core'
import DeviceDeploymentStatus = ddk.cachet.carp.deployments.application.DeviceDeploymentStatus
import StudyDeploymentStatus = ddk.cachet.carp.deployments.application.StudyDeploymentStatus
import ParticipantStatus = ddk.cachet.carp.deployments.application.users.ParticipantStatus
import StudyInvitation = ddk.cachet.carp.deployments.application.users.StudyInvitation

import { dk } from 'carp.core-kotlin-carp.studies.core'
import StudyStatus = dk.cachet.carp.studies.application.StudyStatus
import AssignedParticipantRoles = dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import Participant = dk.cachet.carp.studies.application.users.Participant
import ParticipantGroupStatus = dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import participantIds = dk.cachet.carp.studies.application.users.participantIds
import participantRoles = dk.cachet.carp.studies.application.users.participantRoles
import StudyServiceRequest = dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import RecruitmentServiceRequest = dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest


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

    describe( "StudyServiceRequest", () => {
        it( "can serialize requests with polymorphic serializer", () => {
            const createStudy = new StudyServiceRequest.CreateStudy(
                UUID.Companion.randomUUID(),
                "Test study",
                "This is a study description",
                new StudyInvitation( "Some study" )
            )

            const serialized = JSON.encodeToString( StudyServiceRequest.Serializer, createStudy )
            expect( serialized ).has.string( "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.CreateStudy" )
        } )

        it( "can serialize getStudiesOverview response", () => {
            const status = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", Clock.System.now(), null, true, true, false, true )
            const statusList = listOf( [ status ] )

            const serializer = ListSerializer( StudyStatus.Companion.serializer() )
            expect( serializer ).is.not.undefined
            const serialized = JSON.encodeToString( serializer, statusList )
            expect( serialized ).is.not.not.undefined
        } )
    } )


    describe( "RecruitmentServiceRequest", () => {
        it( "can serialize DeployParticipantGroup", () => {
            const deployGroup = new RecruitmentServiceRequest.InviteNewParticipantGroup(
                UUID.Companion.randomUUID(),
                setOf( [
                    new AssignedParticipantRoles( UUID.Companion.randomUUID(), AssignedTo.All )
                ] )
            )

            const serializer = RecruitmentServiceRequest.Serializer
            const serialized = JSON.encodeToString( serializer, deployGroup )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize Participant", () => {
            const participant = new Participant( new UsernameAccountIdentity( new Username( "Test" ) ), UUID.Companion.randomUUID() )

            const serializer = Participant.Companion.serializer()
            const serialized = JSON.encodeToString( serializer, participant )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize ParticipantGroupStatus", () => {
            const deploymentId = UUID.Companion.randomUUID()
            const now = Clock.System.now()
            const deploymentStatus = new StudyDeploymentStatus.Running( now, deploymentId, listOf<DeviceDeploymentStatus>( [] ), listOf<ParticipantStatus>( [] ), now )
            const participants = setOf( [ new Participant( new UsernameAccountIdentity( new Username( "Test" ) ) ) ] )
            const castDeploymentStatus = deploymentStatus as any // HACK: Type safety needs to be turned off due to loading from different packages.
            const group = new ParticipantGroupStatus.Invited( deploymentId, participants, now, castDeploymentStatus )

            const serializer = ParticipantGroupStatus.Companion.serializer()
            const serialized = JSON.encodeToString( serializer, group )
            expect( serialized ).is.not.undefined
        } )
    } )
} )
