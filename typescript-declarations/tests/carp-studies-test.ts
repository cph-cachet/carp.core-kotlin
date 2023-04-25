import VerifyModule from './VerifyModule'
import { expect } from 'chai'

import { kotlin } from '../src/kotlin'
import setOf = kotlin.collections.setOf
import listOf = kotlin.collections.listOf

import { kotlinx as kcd } from '../src/kotlinx-datetime'
import Clock = kcd.datetime.Clock

import { kotlinx as scd } from '../src/kotlinx-serialization'
import ListSerializer = scd.serialization.builtins.ListSerializer

import { dk } from '../src/carp-studies-core'

import common = dk.cachet.carp.common
import UUID = common.application.UUID
import Username = common.application.users.Username
import AssignedTo = common.application.users.AssignedTo
import UsernameAccountIdentity = common.application.users.UsernameAccountIdentity
import JSON = common.infrastructure.serialization.JSON

import deployments = dk.cachet.carp.deployments
import DeviceDeploymentStatus = deployments.application.DeviceDeploymentStatus
import StudyDeploymentStatus = deployments.application.StudyDeploymentStatus
import ParticipantStatus = deployments.application.users.ParticipantStatus
import StudyInvitation = deployments.application.users.StudyInvitation

import studies = dk.cachet.carp.studies
import StudyStatus = studies.application.StudyStatus
import AssignedParticipantRoles = studies.application.users.AssignedParticipantRoles
import Participant = studies.application.users.Participant
import ParticipantGroupStatus = studies.application.users.ParticipantGroupStatus
import participantIds = studies.application.users.participantIds
import participantRoles = studies.application.users.participantRoles
import StudyServiceRequest = studies.infrastructure.StudyServiceRequest
import RecruitmentServiceRequest = studies.infrastructure.RecruitmentServiceRequest


describe( "carp-studies-core", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = []

        const moduleVerifier = new VerifyModule(
            'carp-studies-core-generated',
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
            const group = new ParticipantGroupStatus.Invited( deploymentId, participants, now, deploymentStatus )

            const serializer = ParticipantGroupStatus.Companion.serializer()
            const serialized = JSON.encodeToString( serializer, group )
            expect( serialized ).is.not.undefined
        } )
    } )
} )
