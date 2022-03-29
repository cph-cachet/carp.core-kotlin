package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.users.ParticipantGroup
import dk.cachet.carp.protocols.application.users.ExpectedParticipantData
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryWithConnectedDeviceProtocol


/**
 * Create a study deployment with a test user assigned to each primary device in the [protocol].
 */
fun studyDeploymentFor( protocol: StudyProtocol ): StudyDeployment
{
    val snapshot = protocol.getSnapshot()

    // Create invitations.
    val identity = UsernameAccountIdentity( "Test user" )
    val invitation = StudyInvitation( "Test" )
    val invitations = protocol.primaryDevices.map {
        ParticipantInvitation( UUID.randomUUID(), setOf( it.roleName ), identity, invitation )
    }

    return StudyDeployment.fromInvitations( snapshot, invitations )
}

/**
 * Creates a study deployment with a registered device and participation added.
 */
fun createComplexDeployment(): StudyDeployment
{
    val protocol = createSinglePrimaryWithConnectedDeviceProtocol( "Primary", "Connected" )
    val deployment = studyDeploymentFor( protocol )

    // Add device registrations.
    val primary = deployment.registrableDevices.first { it.device.roleName == "Primary" }.device as AnyPrimaryDeviceConfiguration
    val connected = deployment.registrableDevices.first { it.device.roleName == "Connected" }.device
    deployment.registerDevice( primary, primary.createRegistration() )
    deployment.registerDevice( connected, connected.createRegistration() )

    // Deploy a device.
    val deviceDeployment = deployment.getDeviceDeploymentFor( primary )
    deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn )

    deployment.stop()

    // Remove events since tests building on top of this are not interested in how this object was constructed.
    deployment.consumeEvents()

    return deployment
}

/**
 * Creates a study deployment that is active (not stopped) for a study protocol with a single primary device.
 */
fun createActiveDeployment( primaryDeviceRoleName: String ): StudyDeployment
{
    val protocol = createSinglePrimaryDeviceProtocol( primaryDeviceRoleName )

    return studyDeploymentFor( protocol )
}

/**
 * Creates a stopped study deployment for a study protocol with a single primary device.
 */
fun createStoppedDeployment( primaryDeviceRoleName: String ): StudyDeployment =
    createActiveDeployment( primaryDeviceRoleName ).apply { stop() }

/**
 * Create a participant invitation for a specific [identity], or newly created identity when null,
 * which is assigned all devices in [protocol].
 */
fun createParticipantInvitation( protocol: StudyProtocol, identity: AccountIdentity? = null ): ParticipantInvitation =
    ParticipantInvitation(
        UUID.randomUUID(),
        protocol.primaryDevices.map { it.roleName }.toSet(),
        identity ?: AccountIdentity.fromUsername( "Test" ),
        StudyInvitation( "Some study" )
    )

/**
 * Creates a participant group with a default and custom expected participant attribute and sets the data.
 */
fun createComplexParticipantGroup(): ParticipantGroup
{
    val protocol: StudyProtocol = createSinglePrimaryDeviceProtocol()
    val defaultExpectedData = ExpectedParticipantData(
        ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
    )
    protocol.addExpectedParticipantData( defaultExpectedData )
    val customExpectedData = ExpectedParticipantData(
        ParticipantAttribute.CustomParticipantAttribute( Text( "Name" ) )
    )
    protocol.addExpectedParticipantData( customExpectedData )
    val deployment = studyDeploymentFor( protocol )

    return ParticipantGroup.fromNewDeployment( deployment ).apply {
        addParticipation(
            Account.withEmailIdentity( "test@test.com" ),
            StudyInvitation( "Some study" ),
            Participation( studyDeploymentId ),
            setOf( protocol.primaryDevices.first() )
        )
        setData( CarpInputDataTypes, CarpInputDataTypes.SEX, Sex.Male )
        setData( CarpInputDataTypes, customExpectedData.inputDataType, CustomInput( "Steven" ) )
        studyDeploymentStopped()
    }
}
