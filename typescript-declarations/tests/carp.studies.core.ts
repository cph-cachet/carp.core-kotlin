import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import ArrayList = kotlin.collections.ArrayList
import HashSet = kotlin.collections.HashSet
import toSet = kotlin.collections.toSet_us0mfu$
import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-runtime'
import Json = kotlinx.serialization.json.Json
import getListSerializer = kotlinx.serialization.get_list_gekvwj$
import { dk as cdk } from 'carp.common'
import DateTime = cdk.cachet.carp.common.DateTime
import UUID = cdk.cachet.carp.common.UUID
import UsernameIdentity = cdk.cachet.carp.common.users.UsernameAccountIdentity
import { dk as ddk } from 'carp.deployment.core'
import StudyInvitation = ddk.cachet.carp.deployment.domain.users.StudyInvitation
import { dk } from 'carp.studies.core'
import AssignParticipantDevices = dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import getAssignedParticipantIds = dk.cachet.carp.studies.domain.users.participantIds_nvx6bb$
import getAssignedDeviceRoles = dk.cachet.carp.studies.domain.users.deviceRoles_nvx6bb$
import Participant = dk.cachet.carp.studies.domain.users.Participant
import StudyOwner = dk.cachet.carp.studies.domain.users.StudyOwner
import StudyStatus = dk.cachet.carp.studies.domain.StudyStatus
import StudyServiceRequest = dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import createStudiesSerializer = dk.cachet.carp.studies.infrastructure.createStudiesSerializer_stpyu4$


describe( "carp.studies.core", () => {
    it( "verify module declarations", async () => {
        const instances = [
            new AssignParticipantDevices( UUID.Companion.randomUUID(), toSet( [ "Test" ] ) ),
            AssignParticipantDevices.Companion,
            new Participant( new UsernameIdentity( "Test" ) ),
            Participant.Companion,
            new StudyOwner(),
            StudyOwner.Companion,
            new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), false, true, false ),
            new StudyStatus.Live( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), true, false ),
            StudyStatus.Companion,
            StudyServiceRequest.Companion
        ]

        const moduleVerifier = new VerifyModule( 'carp.studies.core', instances )
        await moduleVerifier.verify()
    } )


    describe( "AssignParticipantDevices", () => {
        it( "initializes deviceRoleNames as set", () => {
            const uuid = UUID.Companion.randomUUID()
            const deviceRoleNames = toSet( [ "Test" ] )
            const assigned = new AssignParticipantDevices( uuid, deviceRoleNames )
            expect( assigned.deviceRoleNames ).equals( deviceRoleNames )
        } )

        it( "getAssigned participantIds and devices works", () => {
            const participant1 = UUID.Companion.randomUUID()
            const participant2 = UUID.Companion.randomUUID()
            const assigned1 = new AssignParticipantDevices( participant1, toSet( [ "Test" ] ) )
            const assigned2 = new AssignParticipantDevices( participant2, toSet( [ "Test" ] ) )
            const assignedGroup = new ArrayList( [ assigned1, assigned2 ] )
            expect( getAssignedParticipantIds( assignedGroup ) ).instanceof( HashSet )
            expect( getAssignedDeviceRoles( assignedGroup ) ).instanceof( HashSet )
        } )
    } )


    describe( "StudyOwner", () => {
        it( "initializes with default id", () => {
            const owner = new StudyOwner()
            expect( owner.id ).instanceOf( UUID )
        } )
    } )


    describe( "StudyStatus", () => {
        it ( "can typecheck StudyStatus", () => {
            const configuring = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), false, true, false )
            const configuringStatus: StudyStatus = configuring
            expect( configuringStatus instanceof StudyStatus.Configuring ).is.true
            expect( configuringStatus instanceof StudyStatus.Live ).is.false

            const live = new StudyStatus.Live( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), false, true )
            const liveStatus: StudyStatus = live
            expect( liveStatus instanceof StudyStatus.Live ).is.true
            expect( liveStatus instanceof StudyStatus.Configuring ).is.false
        } )
    } )


    describe( "StudyServiceRequest", () => {
        it( "can serialize requests with polymorphic serializer", () => {
            const createStudy = new StudyServiceRequest.CreateStudy(
                new StudyOwner(),
                "Test study",
                StudyInvitation.Companion.empty()
            )

            const json: Json = createStudiesSerializer()
            const serializer = StudyServiceRequest.Companion.serializer()
            const serialized = json.stringify_tf03ej$( serializer, createStudy )
            expect( serialized ).has.string( "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.CreateStudy" )
        } )

        it( "can serialize DeployParticipantGroup", () => {
            const deployGroup = new StudyServiceRequest.DeployParticipantGroup(
                UUID.Companion.randomUUID(),
                toSet( [
                    new AssignParticipantDevices( UUID.Companion.randomUUID(), toSet( [ "Smartphone" ] ) )
                ] )
            )

            const json: Json = createStudiesSerializer()
            const serializer = StudyServiceRequest.Companion.serializer()
            const serialized = json.stringify_tf03ej$( serializer, deployGroup )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize getStudiesOverview response", () => {
            const status = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), false, true, false )
            const statusList = new ArrayList( [ status ] )

            const json: Json = createStudiesSerializer()
            const serializer = getListSerializer( StudyStatus.Companion.serializer() )
            expect( serializer ).is.not.undefined
            const serialized = json.stringify_tf03ej$( serializer, statusList )
            expect( serialized ).is.not.not.undefined
        } )
    } )
} )
