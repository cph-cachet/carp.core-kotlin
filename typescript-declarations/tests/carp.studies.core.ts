import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import ArrayList = kotlin.collections.ArrayList
import HashSet = kotlin.collections.HashSet
import toSet = kotlin.collections.toSet_us0mfu$

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
import Json = kotlinx.serialization.json.Json
import { kotlinx as kotlinxcore } from 'kotlinx-serialization-kotlinx-serialization-core-js-legacy'
import ListSerializer = kotlinxcore.serialization.builtins.ListSerializer_swdriu$

import { kotlinx as kxd } from 'Kotlin-DateTime-library-kotlinx-datetime-js-legacy'
import Clock = kxd.datetime.Clock

import { dk as cdk } from 'carp.core-kotlin-carp.common'
import UUID = cdk.cachet.carp.common.application.UUID
import CarpInputDataTypes = cdk.cachet.carp.common.application.data.input.CarpInputDataTypes
import Username = cdk.cachet.carp.common.application.users.Username
import AssignedTo = cdk.cachet.carp.common.application.users.AssignedTo
import UsernameAccountIdentity = cdk.cachet.carp.common.application.users.UsernameAccountIdentity
import createDefaultJSON = cdk.cachet.carp.common.infrastructure.serialization.createDefaultJSON_18xi4u$

import { dk as ddk } from 'carp.core-kotlin-carp.deployments.core'
import DeviceDeploymentStatus = ddk.cachet.carp.deployments.application.DeviceDeploymentStatus
import StudyDeploymentStatus = ddk.cachet.carp.deployments.application.StudyDeploymentStatus
import ParticipantStatus = ddk.cachet.carp.deployments.application.users.ParticipantStatus
import StudyInvitation = ddk.cachet.carp.deployments.application.users.StudyInvitation

import { dk } from 'carp.core-kotlin-carp.studies.core'
import StudyDetails = dk.cachet.carp.studies.application.StudyDetails
import StudyStatus = dk.cachet.carp.studies.application.StudyStatus
import AssignParticipantRoles = dk.cachet.carp.studies.application.users.AssignParticipantRoles
import Participant = dk.cachet.carp.studies.application.users.Participant
import ParticipantGroupStatus = dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import getAssignedParticipantIds = dk.cachet.carp.studies.application.users.participantIds_yyd5wh$
import getAssignedParticipantRoles = dk.cachet.carp.studies.application.users.participantRoles_yyd5wh$
import RecruitmentServiceRequest = dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import StudyServiceRequest = dk.cachet.carp.studies.infrastructure.StudyServiceRequest


describe( "carp.studies.core", () => {
    it( "verify module declarations", async () => {
        const deploymentId = UUID.Companion.randomUUID()
        const now = Clock.System.now()
        const invitedDeploymentStatus = new StudyDeploymentStatus.Invited( now, deploymentId, new ArrayList<DeviceDeploymentStatus>( [] ), new ArrayList<ParticipantStatus>( [] ), null )

        const instances = [
            new StudyDetails( UUID.Companion.randomUUID(), UUID.Companion.randomUUID(), "Name", Clock.System.now(), "Description", new StudyInvitation( "Some study" ), null ),
            StudyDetails.Companion,
            [ "StudyStatus", new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", Clock.System.now(), null, true, true, false, true ) ],
            new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", Clock.System.now(), null, true, true, false, true ),
            new StudyStatus.Live( UUID.Companion.randomUUID(), "Test", Clock.System.now(), UUID.Companion.randomUUID(), false, false, true ),
            StudyStatus.Companion,
            new AssignParticipantRoles( UUID.Companion.randomUUID(), AssignedTo.All ),
            AssignParticipantRoles.Companion,
            new Participant( new UsernameAccountIdentity( new Username( "Test" ) ) ),
            Participant.Companion,
            [ "ParticipantGroupStatus", new ParticipantGroupStatus.Staged( deploymentId, new HashSet<Participant>() ) ],
            new ParticipantGroupStatus.Staged( deploymentId, new HashSet<Participant>() ),
            [ "ParticipantGroupStatus$InDeployment", new ParticipantGroupStatus.Invited( deploymentId, new HashSet<Participant>(), now, invitedDeploymentStatus ) ],
            new ParticipantGroupStatus.Invited( deploymentId, new HashSet<Participant>(), now, invitedDeploymentStatus ),
            new ParticipantGroupStatus.Running( deploymentId, new HashSet<Participant>(), now, invitedDeploymentStatus, now ),
            new ParticipantGroupStatus.Stopped( deploymentId, new HashSet<Participant>(), now, invitedDeploymentStatus, null, now ),
            ParticipantGroupStatus.Companion,
            [ "StudyServiceRequest", new StudyServiceRequest.Remove( UUID.Companion.randomUUID() ) ],
            [ "RecruitmentServiceRequest", new RecruitmentServiceRequest.GetParticipants( UUID.Companion.randomUUID() ) ]
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.studies.core', instances )
        await moduleVerifier.verify()
    } )


    describe( "AssignParticipantRoles", () => {
        it( "getAssigned participantIds and participantRoles works", () => {
            const participant1 = UUID.Companion.randomUUID()
            const participant2 = UUID.Companion.randomUUID()
            const assigned1 = new AssignParticipantRoles( participant1, new AssignedTo.Roles( toSet( [ "Test" ] ) ) )
            const assigned2 = new AssignParticipantRoles( participant2, AssignedTo.All )
            const assignedGroup = new ArrayList( [ assigned1, assigned2 ] )
            expect( getAssignedParticipantIds( assignedGroup ) ).instanceof( HashSet )
            expect( getAssignedParticipantRoles( assignedGroup ) ).instanceof( HashSet )
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

            const json: Json = createDefaultJSON()
            const serializer = StudyServiceRequest.Serializer
            const serialized = json.encodeToString_tf03ej$( serializer, createStudy )
            expect( serialized ).has.string( "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.CreateStudy" )
        } )

        it( "can serialize getStudiesOverview response", () => {
            const status = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", Clock.System.now(), null, true, true, false, true )
            const statusList = new ArrayList( [ status ] )

            const json: Json = createDefaultJSON()
            const serializer = ListSerializer( StudyStatus.Companion.serializer() )
            expect( serializer ).is.not.undefined
            const serialized = json.encodeToString_tf03ej$( serializer, statusList )
            expect( serialized ).is.not.not.undefined
        } )
    } )


    describe( "RecruitmentServiceRequest", () => {
        it( "can serialize DeployParticipantGroup", () => {
            const deployGroup = new RecruitmentServiceRequest.InviteNewParticipantGroup(
                UUID.Companion.randomUUID(),
                toSet( [
                    new AssignParticipantRoles( UUID.Companion.randomUUID(), AssignedTo.All )
                ] )
            )

            const json: Json = createDefaultJSON()
            const serializer = RecruitmentServiceRequest.Serializer
            const serialized = json.encodeToString_tf03ej$( serializer, deployGroup )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize Participant", () => {
            const participant = new Participant( new UsernameAccountIdentity( new Username( "Test" ) ), UUID.Companion.randomUUID() )

            const json: Json = createDefaultJSON()
            const serializer = Participant.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, participant )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize ParticipantGroupStatus", () => {
            const deploymentId = UUID.Companion.randomUUID()
            const now = Clock.System.now()
            const deploymentStatus = new StudyDeploymentStatus.Running( now, deploymentId, new ArrayList<DeviceDeploymentStatus>( [] ), new ArrayList<ParticipantStatus>( [] ), now )
            const participants = toSet( [ new Participant( new UsernameAccountIdentity( new Username( "Test" ) ) ) ] )
            const group = new ParticipantGroupStatus.Invited( deploymentId, participants, now, deploymentStatus )

            const json: Json = createDefaultJSON()
            const serializer = ParticipantGroupStatus.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, group )
            expect( serialized ).is.not.undefined
        } )
    } )
} )
