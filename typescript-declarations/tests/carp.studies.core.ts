import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { dk as cdk } from 'carp.common'
import DateTime = cdk.cachet.carp.common.DateTime
import UUID = cdk.cachet.carp.common.UUID
import { dk } from 'carp.studies.core'
import StudyOwner = dk.cachet.carp.studies.domain.users.StudyOwner
import StudyStatus = dk.cachet.carp.studies.domain.StudyStatus


describe( "carp.studies.core", () => {
    it( "verify module declarations", async () => {
        const instances = [
            new StudyOwner(),
            StudyOwner.Companion,
            new StudyStatus( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), false, false ),
            StudyStatus.Companion
        ]

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
