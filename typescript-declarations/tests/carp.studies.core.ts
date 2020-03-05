import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { dk as cdk } from 'carp.common'
import UUID = cdk.cachet.carp.common.UUID
import { dk } from 'carp.studies.core'
import StudyOwner = dk.cachet.carp.studies.domain.users.StudyOwner


describe( "carp.studies.core", () => {
    it( "verify module declarations", async () => {
        const instances = new Map<string, any>( [
            [ "StudyOwner", new StudyOwner() ],
            [ "StudyOwner$Companion", StudyOwner.Companion ]
        ] )

        const moduleVerifier = new VerifyModule( 'carp.studies.core', instances )
        await moduleVerifier.verify()
    } )


    describe( "StudyOwner", () => {
        it( "initializes with default id", () => {
            const owner = new StudyOwner()
            expect( owner.id ).instanceOf( UUID )
        } )
    } )
} )
