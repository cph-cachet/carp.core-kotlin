import VerifyModule from './VerifyModule'

import { expect } from 'chai'
import { kotlin } from '../src/kotlin'
import setOf = kotlin.collections.setOf
import { dk } from "../src/carp-common"
import Trilean = dk.cachet.carp.common.application.Trilean
import toTrilean = dk.cachet.carp.common.application.toTrilean
import EmailAddress = dk.cachet.carp.common.application.EmailAddress
import NamespacedId = dk.cachet.carp.common.application.NamespacedId
import AccountIdentity = dk.cachet.carp.common.application.users.AccountIdentity
import AssignedTo = dk.cachet.carp.common.application.users.AssignedTo
import EmailAccountIdentity = dk.cachet.carp.common.application.users.EmailAccountIdentity
import ExpectedParticipantData = dk.cachet.carp.common.application.users.ExpectedParticipantData
import ParticipantAttribute = dk.cachet.carp.common.application.users.ParticipantAttribute
import CarpInputDataTypes = dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import Text = dk.cachet.carp.common.application.data.input.elements.Text
import CustomInput = dk.cachet.carp.common.application.data.input.CustomInput


describe( "carp.common", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = []

        const moduleVerifier = new VerifyModule(
            'carp.core-kotlin-carp.common',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "Trilean", () => {
        it( "has values TRUE, FALSE, UNKNOWN", () => {
            const trileanValues = Trilean.values()
            expect( trileanValues ).to.have.members( [ Trilean.TRUE, Trilean.FALSE, Trilean.UNKNOWN ] )
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

        it( "can initialize from string", () => {
            const identity = EmailAccountIdentity.create( "test@test.com" )
            expect( identity.emailAddress ).instanceOf( EmailAddress )
        } )

        it( "can cast to AccountIdentity base type", () => {
            const identity: AccountIdentity = EmailAccountIdentity.create( "test@test.com" )
        } )
    } )

    describe( "ParticipantAttribute", () => {
        const attribute = new ParticipantAttribute.CustomParticipantAttribute( new Text( "Prompt" ) )

        it( "getInputElement works", () => {
            const inputElement = attribute.getInputElement( CarpInputDataTypes )
            expect( inputElement ).instanceOf( Text )
        } )

        it( "isValidInput works", () => {
            const isNumberValid = attribute.isValidInput( CarpInputDataTypes, 42 )
            expect( isNumberValid ).is.false

            const isStringValid = attribute.isValidInput( CarpInputDataTypes, "Steven" )
            expect( isStringValid ).is.true
        } )

        it( "inputToData works", () => {
            const data = attribute.inputToData( CarpInputDataTypes, "Steven" )
            const customInput = data as CustomInput
            expect( customInput.input ).equals( "Steven" )
        } )
    } )
} )
