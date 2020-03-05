import VerifyModule from './VerifyModule'

import { dk } from 'carp.deployment.core'
import StudyInvitation = dk.cachet.carp.deployment.domain.users.StudyInvitation


describe( "carp.deployment.core", () => {
    it( "verify module declarations", async () => {
        const instances = [
            StudyInvitation.Companion.empty(),
            StudyInvitation.Companion
        ]

        const moduleVerifier = new VerifyModule( 'carp.deployment.core', instances )
        await moduleVerifier.verify()
    } )
} )
