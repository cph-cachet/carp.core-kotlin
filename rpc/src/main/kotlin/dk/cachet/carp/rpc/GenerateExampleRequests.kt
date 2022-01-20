@file:Suppress( "WildcardImport", "MagicNumber", "BooleanLiteralArgument", "TopLevelPropertyNaming" )

package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.*
import dk.cachet.carp.common.application.data.*
import dk.cachet.carp.common.application.data.input.*
import dk.cachet.carp.common.application.devices.*
import dk.cachet.carp.common.application.sampling.*
import dk.cachet.carp.common.application.services.*
import dk.cachet.carp.common.application.tasks.*
import dk.cachet.carp.common.application.triggers.*
import dk.cachet.carp.common.application.users.*
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.data.application.*
import dk.cachet.carp.data.infrastructure.*
import dk.cachet.carp.deployments.application.*
import dk.cachet.carp.deployments.application.users.*
import dk.cachet.carp.deployments.infrastructure.*
import dk.cachet.carp.protocols.application.*
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.infrastructure.*
import dk.cachet.carp.studies.application.*
import dk.cachet.carp.studies.application.users.*
import dk.cachet.carp.studies.infrastructure.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds


/**
 * Generate a single [ExampleRequest] using the corresponding request object for each of the methods in
 * application service [AS].
 */
@OptIn( InternalSerializationApi::class )
fun <AS : ApplicationService<AS, *>> generateExampleRequests(
    applicationServiceInterface: Class<out ApplicationService<AS, *>>,
    requestObjectSuperType: Class<*>
): List<ExampleRequest<AS>>
{
    val requests = applicationServiceInterface.methods
    val requestObjects = requestObjectSuperType.classes

    val json = Json( createDefaultJSON() ) { prettyPrint = true }
    @Suppress( "UNCHECKED_CAST" )
    val requestObjectSerializer = requestObjectSuperType.kotlin.serializer() as KSerializer<Any>

    return requests.map { request ->
        val requestName = applicationServiceInterface.name + "." + request.name
        val requestObjectName = request.name.replaceFirstChar { it.uppercase() }
        val requestObject = requestObjects.singleOrNull { it.simpleName == requestObjectName }
        checkNotNull( requestObject )
            {
                "Could not find request object for $requestName. " +
                "Searched for: ${requestObjectSuperType.name}.$requestObjectName"
            }

        // Retrieve example and verify whether it is valid.
        val example = checkNotNull( exampleRequests[ request.kotlinFunction ] )
            { "No example request and response instances provided for $requestName." }
        check( requestObject.isInstance( example.request ) )
            { "Incorrect request instance provided for $requestName." }
        check( request.returnType.isInstance( example.response ) )
            { "Incorrect response instance provided for $requestName." }

        // Create example JSON request and response.
        val requestObjectJson = json.encodeToString( requestObjectSerializer, example.request )
        val responseJson = json.encodeToString( example.getResponseSerializer( request ), example.response )

        ExampleRequest(
            applicationServiceInterface,
            request,
            ExampleRequest.JsonExample( requestObject, requestObjectJson ),
            ExampleRequest.JsonExample( request.returnType, responseJson )
        )
    }
}


// Example protocol with a single smartphone.
private val ownerId = UUID( "491f03fc-964b-4783-86a6-a528bbfe4e94" )
private val protocolId = UUID( "25fe92a5-0d52-4e37-8d05-31f347d72d3d" )
private val protocolCreatedOn = Instant.fromEpochSeconds( 1642503419 )
private val phone = Smartphone( "Participant's phone", false ) {
    defaultSamplingConfiguration {
        geolocation { batteryNormal { granularity = Granularity.Detailed } }
    }
}
private val bikeBeacon = AltBeacon( "Participant's bike", true )
private val measurePhoneMovement = BackgroundTask(
    "Monitor movement",
    listOf( Smartphone.Sensors.GEOLOCATION.measure { }, Smartphone.Sensors.STEP_COUNT.measure { } ),
    "Track step count and geolocation for one week.",
    7.days
)
private val measureBikeProximity = BackgroundTask(
    "Monitor proximity to bike",
    listOf( AltBeacon.Sensors.SIGNAL_STRENGTH.measure { } ),
    null,
    7.days
)
private val startOfStudyTrigger = phone.atStartOfStudy()
private val phoneProtocol = StudyProtocol(
    ownerId,
    "Nonmotorized transport study",
    "Track how much nonmotorized movement participants perform.",
    protocolId,
    protocolCreatedOn
).apply {
    addMasterDevice( phone )
    addConnectedDevice( bikeBeacon, phone )
    addTaskControl( startOfStudyTrigger.start( measurePhoneMovement, phone ) )
    addTaskControl( startOfStudyTrigger.start( measureBikeProximity, bikeBeacon ) )
    applicationData = "{\"uiTheme\": \"black\"}"
}.getSnapshot()
private val startOfStudyTriggerId = phoneProtocol.triggers.entries.first { it.value == startOfStudyTrigger }.key
private val expectedParticipantData = setOf<ParticipantAttribute>(
    ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
)

// Example protocol factory protocols.
private val customProtocol = runBlocking {
    ProtocolFactoryServiceHost().createCustomProtocol(
        ownerId,
        "Fictional Company study",
        """{"collect-data": "heartrate, gps"}""",
        "Collect heartrate and GPS using Fictional Company's software."
    )
}

// Study matching the example protocol.
private val studyId = UUID( "791fd191-4279-482f-9ef5-5b4508efd959" )
private const val studyName = "Copenhagen transportation study"
private const val studyDescription = "Track how people walk/bike in Copenhagen."
private val studyCreatedOn = Instant.fromEpochSeconds( 1642503800 )
private val studyConfiguringStatus = StudyStatus.Configuring( studyId, studyName, studyCreatedOn, true, true, false, false )
private val studyLiveStatus = StudyStatus.Live( studyId, studyName, studyCreatedOn, false, false, true )

// Deployment data matching the example protocol.
private val deploymentId = UUID( "c9cc5317-48da-45f2-958e-58bc07f34681" )
private val deploymentIds = setOf( deploymentId, UUID( "d4a9bba4-860e-4c58-a356-8a91605dc1ee" ) )
private val deploymentCreatedOn = Instant.fromEpochSeconds( 1642504000 )
private val participantId = UUID( "32880e82-01c9-40cf-a6ed-17ff3348f251" )
private val participantAccount = EmailAccountIdentity( "boaty@mcboatface.com" )
private val participantAccountId = UUID( "ca60cb7f-de18-44b6-baf9-3c8e6a73005a" )
private val studyInvitation = StudyInvitation(
    studyName,
    "Participate in this study, which keeps track of how much you walk and bike!",
    "{\"trialGroup\", \"A\"}"
)
private val participantInvitation = ParticipantInvitation( participantId, setOf( phone.roleName ), participantAccount, studyInvitation )
private val participantData = ParticipantData(
    deploymentId,
    mapOf( CarpInputDataTypes.SEX to Sex.Male )
)
private val bikeBeaconPreregistration = bikeBeacon.createRegistration {
    manufacturerId = 0x118
    organizationId = UUID( "4e990957-0838-414c-bf25-2d391e2990b5" )
    majorId = 42
    minorId = 42
}
private val phoneRegistration = phone.createRegistration {
    deviceId = UUID( "fc7b41b0-e9e2-4b5d-8c3d-5119b556a3f0" ).toString()
}
private val bikeBeaconStatus = DeviceDeploymentStatus.Registered( bikeBeacon, false, emptySet(), emptySet() )
private val participantsStatus = listOf( ParticipantStatus( participantId, setOf( phone.roleName ) ) )
private val invitedDeploymentStatus = StudyDeploymentStatus.Invited(
    deploymentCreatedOn,
    deploymentId,
    listOf(
        DeviceDeploymentStatus.Unregistered( phone, true, setOf( phone.roleName ), emptySet() ),
        bikeBeaconStatus
    ),
    participantsStatus,
    null
)
private val runningDeploymentStatus = StudyDeploymentStatus.Running(
    deploymentCreatedOn,
    deploymentId,
    listOf(
        DeviceDeploymentStatus.Deployed( phone ),
        bikeBeaconStatus
    ),
    participantsStatus,
    Instant.fromEpochSeconds( 1642504500 )
)
private val stoppedDeploymentStatus = StudyDeploymentStatus.Stopped(
    deploymentCreatedOn,
    deploymentId,
    listOf(
        DeviceDeploymentStatus.Deployed( phone ),
        bikeBeaconStatus
    ),
    participantsStatus,
    runningDeploymentStatus.startedOn,
    Instant.fromEpochSeconds( 1642506000 )
)
private val participants = setOf( Participant( participantAccount, participantId ) )
private val participantGroupInvitedOn = Instant.fromEpochSeconds( 1642514010 )
private val phoneDeviceDeployment = MasterDeviceDeployment(
    phone,
    phoneRegistration,
    setOf( bikeBeacon ),
    mapOf( bikeBeacon.roleName to bikeBeaconPreregistration ),
    setOf( measurePhoneMovement, measureBikeProximity ),
    mapOf( startOfStudyTriggerId to phone.atStartOfStudy() ),
    setOf(
        TaskControl( startOfStudyTriggerId, measurePhoneMovement.name, phone.roleName, TaskControl.Control.Start ),
        TaskControl( startOfStudyTriggerId, measureBikeProximity.name, bikeBeacon.roleName, TaskControl.Control.Start )
    ),
    phoneProtocol.applicationData
)

// Data matching the example protocol.
private val phoneGeoDataStream = dataStreamId<Geolocation>( deploymentId, phone.roleName )
private val phoneStepsDataStream = dataStreamId<StepCount>( deploymentId, phone.roleName )
private val expectedDataStreams = setOf(
    DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId( phoneGeoDataStream ),
    DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId( phoneStepsDataStream )
)
private val geoDataSequence =
    MutableDataStreamSequence( phoneGeoDataStream, 0, listOf( startOfStudyTriggerId ) ).apply {
        appendMeasurements( measurement( Geolocation( 55.68061908805645, 12.582050313435703 ), 1642505045000000L ) )
        appendMeasurements( measurement( Geolocation( 55.680802203873114, 12.581802212861367 ), 1642505144000000L ) )
    }
private val stepsDataSequence =
    MutableDataStreamSequence( phoneStepsDataStream, 0, listOf( startOfStudyTriggerId ) ).apply {
        appendMeasurements( measurement( StepCount( 0 ), 1642505045000000L ) )
        appendMeasurements( measurement( StepCount( 30 ), 1642505144000000L ) )
    }
private val phoneDataStreamBatch = MutableDataStreamBatch().apply {
    appendSequence( geoDataSequence )
    appendSequence( stepsDataSequence )
}


private class Example(
    val request: Any,
    val response: Any = Unit,
    val overrideResponseSerializer: KSerializer<*>? = null
)
{
    @Suppress( "UNCHECKED_CAST" )
    fun getResponseSerializer( request: Method ): KSerializer<Any> = getSerializer( request ) as KSerializer<Any>

    @OptIn( InternalSerializationApi::class )
    private fun getSerializer( request: Method ): KSerializer<*>
    {
        if ( overrideResponseSerializer != null ) return overrideResponseSerializer

        val returnType = request.kotlinFunction!!.returnType
        return serializer( returnType )
    }
}

private val exampleRequests: Map<KFunction<*>, Example> = mapOf(
    // ProtocolService
    ProtocolService::add to Example(
        request = ProtocolServiceRequest.Add( phoneProtocol, "Version 1" )
    ),
    ProtocolService::addVersion to Example(
        request = ProtocolServiceRequest.AddVersion( phoneProtocol.copy( name = "Walking/biking study" ), "Version 2: new name" )
    ),
    ProtocolService::updateParticipantDataConfiguration to Example(
        request = ProtocolServiceRequest.UpdateParticipantDataConfiguration( protocolId, "Version 3: ask participant data", expectedParticipantData ),
        response = phoneProtocol.copy( expectedParticipantData = expectedParticipantData )
    ),
    ProtocolService::getBy to Example(
        request = ProtocolServiceRequest.GetBy( protocolId, "Version 1" ),
        response = phoneProtocol
    ),
    ProtocolService::getAllForOwner to Example(
        request = ProtocolServiceRequest.GetAllForOwner( ownerId ),
        response = listOf( phoneProtocol )
    ),
    ProtocolService::getVersionHistoryFor to Example(
        request = ProtocolServiceRequest.GetVersionHistoryFor( protocolId ),
        response = listOf(
            ProtocolVersion( "Version 1", protocolCreatedOn ),
            ProtocolVersion( "Version 2: new name", protocolCreatedOn + 10.seconds ),
            ProtocolVersion( "Version 3: ask participant data", protocolCreatedOn + 20.seconds )
        )
    ),

    // ProtocolFactoryService
    ProtocolFactoryService::createCustomProtocol to Example(
        request = ProtocolFactoryServiceRequest.CreateCustomProtocol(
            ownerId,
            customProtocol.name,
            customProtocol.tasks.filterIsInstance<CustomProtocolTask>().single().studyProtocol,
            customProtocol.description
        ),
        response = customProtocol
    ),

    // StudyService
    StudyService::createStudy to Example(
        request = StudyServiceRequest.CreateStudy( ownerId, studyName, studyDescription, studyInvitation ),
        response = studyConfiguringStatus
    ),
    StudyService::setInternalDescription to Example(
        request = StudyServiceRequest.SetInternalDescription( studyId, "Copenhagen/Denmark transportation study", studyDescription ),
        response = studyConfiguringStatus.copy( name = "Copenhagen/Denmark transportation study" )
    ),
    StudyService::getStudyDetails to Example(
        request = StudyServiceRequest.GetStudyDetails( studyId ),
        response = StudyDetails( studyId, ownerId, studyName, studyCreatedOn, studyDescription, studyInvitation, phoneProtocol )
    ),
    StudyService::getStudyStatus to Example(
        request = StudyServiceRequest.GetStudyStatus( studyId ),
        response = studyLiveStatus
    ),
    StudyService::getStudiesOverview to Example(
        request = StudyServiceRequest.GetStudiesOverview( ownerId ),
        response = listOf(
            studyConfiguringStatus,
            StudyStatus.Live( UUID( "3566eb9c-1d2f-4ed9-bf8a-8ea43638773d" ), "Heartrate study", Instant.fromEpochSeconds( 1642514000 ), false, false, true )
        )
    ),
    StudyService::setInvitation to Example(
        request = StudyServiceRequest.SetInvitation( studyId, studyInvitation ),
        response = studyConfiguringStatus
    ),
    StudyService::setProtocol to Example(
        request = StudyServiceRequest.SetProtocol( studyId, phoneProtocol ),
        response = StudyStatus.Configuring( studyId, studyName, studyCreatedOn, true, true, false, true )
    ),
    StudyService::goLive to Example(
        request = StudyServiceRequest.GoLive( studyId ),
        response = studyLiveStatus
    ),
    StudyService::remove to Example(
        request = StudyServiceRequest.Remove( studyId ),
        response = true
    ),

    // RecruitmentService
    RecruitmentService::addParticipant to Example(
        request = RecruitmentServiceRequest.AddParticipant( studyId, participantAccount.emailAddress ),
        response = Participant( participantAccount, participantId )
    ),
    RecruitmentService::getParticipant to Example(
        request = RecruitmentServiceRequest.GetParticipant( studyId, participantId ),
        response = Participant( participantAccount, participantId )
    ),
    RecruitmentService::getParticipants to Example(
        request = RecruitmentServiceRequest.GetParticipants( studyId ),
        response = listOf(
            Participant( participantAccount, participantId ),
            Participant( UsernameAccountIdentity( "John Doe" ), UUID( "d7436912-ac9f-4f9b-a29e-376af8a0fbb4" ) )
        )
    ),
    RecruitmentService::inviteNewParticipantGroup to Example(
        request = RecruitmentServiceRequest.InviteNewParticipantGroup( studyId, setOf( AssignParticipantDevices( participantId, setOf( phone.roleName ) )) ),
        response = ParticipantGroupStatus.Invited( deploymentId, participants, participantGroupInvitedOn, invitedDeploymentStatus )
    ),
    RecruitmentService::getParticipantGroupStatusList to Example(
        request = RecruitmentServiceRequest.GetParticipantGroupStatusList( studyId ),
        response = listOf( ParticipantGroupStatus.Running( deploymentId, participants, participantGroupInvitedOn, runningDeploymentStatus, runningDeploymentStatus.startedOn ) )
    ),
    RecruitmentService::stopParticipantGroup to Example(
        request = RecruitmentServiceRequest.StopParticipantGroup( studyId, deploymentId ),
        response = ParticipantGroupStatus.Stopped( deploymentId, participants, participantGroupInvitedOn, stoppedDeploymentStatus, stoppedDeploymentStatus.startedOn, stoppedDeploymentStatus.stoppedOn )
    ),

    // DeploymentService
    DeploymentService::createStudyDeployment to Example(
        request = DeploymentServiceRequest.CreateStudyDeployment(
            deploymentId,
            phoneProtocol,
            listOf( participantInvitation ),
            mapOf( bikeBeacon.roleName to bikeBeaconPreregistration )
        ),
        response = invitedDeploymentStatus
    ),
    DeploymentService::removeStudyDeployments to Example(
        request = DeploymentServiceRequest.RemoveStudyDeployments( deploymentIds ),
        response = setOf( deploymentId )
    ),
    DeploymentService::getStudyDeploymentStatus to Example(
        request = DeploymentServiceRequest.GetStudyDeploymentStatus( deploymentId ),
        response = invitedDeploymentStatus
    ),
    DeploymentService::getStudyDeploymentStatusList to Example(
        request = DeploymentServiceRequest.GetStudyDeploymentStatusList( setOf( deploymentId ) ),
        response = listOf( invitedDeploymentStatus )
    ),
    DeploymentService::registerDevice to Example(
        request = DeploymentServiceRequest.RegisterDevice( deploymentId, phone.roleName, phoneRegistration ),
        response = StudyDeploymentStatus.DeployingDevices(
            deploymentCreatedOn,
            deploymentId,
            listOf(
                DeviceDeploymentStatus.Registered( phone, true, emptySet(), emptySet() ),
                bikeBeaconStatus
            ),
            participantsStatus,
            null
        )
    ),
    DeploymentService::unregisterDevice to Example(
        request = DeploymentServiceRequest.UnregisterDevice( deploymentId, phone.roleName ),
        response = invitedDeploymentStatus
    ),
    DeploymentService::getDeviceDeploymentFor to Example(
        request = DeploymentServiceRequest.GetDeviceDeploymentFor( deploymentId, phone.roleName ),
        response = phoneDeviceDeployment
    ),
    DeploymentService::deviceDeployed to Example(
        request = DeploymentServiceRequest.DeviceDeployed( deploymentId, phone.roleName, phoneDeviceDeployment.lastUpdatedOn ),
        response = runningDeploymentStatus
    ),
    DeploymentService::stop to Example(
        request = DeploymentServiceRequest.Stop( deploymentId ),
        response = stoppedDeploymentStatus
    ),

    // ParticipationService
    ParticipationService::getActiveParticipationInvitations to Example(
        request = ParticipationServiceRequest.GetActiveParticipationInvitations( participantAccountId ),
        response = setOf(
            ActiveParticipationInvitation(
                Participation( deploymentId, participantId ),
                studyInvitation,
                setOf( AssignedMasterDevice( phone ) )
            )
        )
    ),
    ParticipationService::getParticipantData to Example(
        request = ParticipationServiceRequest.GetParticipantData( deploymentId ),
        response = participantData
    ),
    ParticipationService::getParticipantDataList to Example(
        request = ParticipationServiceRequest.GetParticipantDataList( setOf( deploymentId ) ),
        response = listOf( participantData )
    ),
    ParticipationService::setParticipantData to Example(
        request = ParticipationServiceRequest.SetParticipantData( deploymentId, participantData.data ),
        response = participantData
    ),

    // DataStreamService
    DataStreamService::openDataStreams to Example(
        request = DataStreamServiceRequest.OpenDataStreams( DataStreamsConfiguration( deploymentId, expectedDataStreams ) )
    ),
    DataStreamService::appendToDataStreams to Example(
        request = DataStreamServiceRequest.AppendToDataStreams( deploymentId, phoneDataStreamBatch )
    ),
    DataStreamService::getDataStream to Example(
        request = DataStreamServiceRequest.GetDataStream( phoneGeoDataStream, 0, 100 ),
        response = MutableDataStreamBatch().apply { appendSequence( geoDataSequence ) },
        overrideResponseSerializer = DataStreamBatchSerializer
    ),
    DataStreamService::closeDataStreams to Example(
        request = DataStreamServiceRequest.CloseDataStreams( deploymentIds )
    ),
    DataStreamService::removeDataStreams to Example(
        request = DataStreamServiceRequest.RemoveDataStreams( deploymentIds ),
        response = true
    )
)
