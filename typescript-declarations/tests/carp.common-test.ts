import VerifyModule from './VerifyModule'

import { expect } from 'chai'
import { kotlin } from '../src/kotlin'
import setOf = kotlin.collections.setOf
import { kotlinx } from '../src/kotlinx-serialization'
import Json = kotlinx.serialization.json.Json
import { dk } from '../src/carp-common'
import EmailAddress = dk.cachet.carp.common.application.EmailAddress
import NamespacedId = dk.cachet.carp.common.application.NamespacedId
import Trilean = dk.cachet.carp.common.application.Trilean
import toTrilean = dk.cachet.carp.common.application.toTrilean
import CarpInputDataTypes = dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import Text = dk.cachet.carp.common.application.data.input.elements.Text
import EmailAccountIdentity = dk.cachet.carp.common.application.users.EmailAccountIdentity
import ExpectedParticipantData = dk.cachet.carp.common.application.users.ExpectedParticipantData
import ExpectedParticipantDataSerializer = dk.cachet.carp.common.application.users.ExpectedParticipantDataSerializer
import AssignedTo = dk.cachet.carp.common.application.users.AssignedTo
import ParticipantAttribute = dk.cachet.carp.common.application.users.ParticipantAttribute
import createDefaultJSON = dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON


describe( "carp.common", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            [ "InputElement", new Text( "Prompt" ) ],
            [ "AssignedTo", AssignedTo.All ],
            [ "ParticipantAttribute", new ParticipantAttribute.CustomParticipantAttribute( new Text( "Name" ) ) ],
        ]

        const moduleVerifier = new VerifyModule(
            'carp.core-kotlin-carp.common',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "Trilean", () => {
        it( "has constant values TRUE, FALSE, UNKNOWN", () => {
            const trueTrilean = Trilean.TRUE
            expect( Trilean.TRUE ).equals( trueTrilean )

            const falseTrilean = Trilean.FALSE
            expect( Trilean.FALSE ).equals( falseTrilean )

            const unknownTrilean = Trilean.UNKNOWN
            expect( Trilean.UNKNOWN ).equals( unknownTrilean )
        } )

        it( "toTrilean works", () => {
            expect( toTrilean( true ) ).equals( Trilean.TRUE )
            expect( toTrilean( false ) ).equals( Trilean.FALSE )
        } )
    } )

    describe( "EmailAccountIdentity", () => {
        it( "can initialize from email address", () => {
            const identity = new EmailAccountIdentity( new EmailAddress( "test@test.com" ) )
            expect( identity.emailAddress ).instanceOf( EmailAddress )
        } )

        it( "static create method is broken", () => {
            // This verifies whether a TypeScript generation error still exists:
            // https://youtrack.jetbrains.com/issue/KT-52587/KJS-IR-static-constructor-exported-in-wrong-location-in-JS-sources
            expect( EmailAccountIdentity.create ).is.undefined
        } )
    } )

    describe( "ParticipantAttribute", () => {
        const attribute = new ParticipantAttribute.CustomParticipantAttribute( new Text( "Prompt" ) )

        it( "getInputElement works", () => {
            const inputElement = attribute.getInputElement_kussp3_k$( CarpInputDataTypes )
            expect( inputElement ).instanceOf( Text )
        } )

        it( "isValidInput works", () => {
            const isNumberValid = attribute.isValidInput_xr0dfw_k$( CarpInputDataTypes, 42 )
            expect( isNumberValid ).is.false

            const isStringValid = attribute.isValidInput_xr0dfw_k$( CarpInputDataTypes, "Steven" )
            expect( isStringValid ).is.true
        } )

        it( "inputToData works", () => {
            const data = attribute.inputToData_xr0dfw_k$( CarpInputDataTypes, "Steven" )
            expect( data ).is.not.undefined
        } )
    } )

    describe( "ExpectedParticipantData", () => {
        it( "static Companion is broken", () => {
            // This verifies whether a TypeScript generation error still exists. Likely same bug as:
            // https://youtrack.jetbrains.com/issue/KT-52587/KJS-IR-static-constructor-exported-in-wrong-location-in-JS-sources
            const expectedData = new ExpectedParticipantData(
                new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ),
                AssignedTo.All
            )

            expect( expectedData.Companion ).is.undefined
        } )

        it( "can serialize polymorphic AssignedTo", () => {
            const expectedData = new ExpectedParticipantData(
                new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ),
                new AssignedTo.Roles( setOf( [ "Roles are added" ] ) )
            )

            const json: Json = createDefaultJSON( null )
            const serializer = ExpectedParticipantDataSerializer
            const serialized = json.encodeToString_onvojc_k$( serializer, expectedData )
            expect( serialized ).has.string( "Roles are added" )
        } )
    } )
} )
