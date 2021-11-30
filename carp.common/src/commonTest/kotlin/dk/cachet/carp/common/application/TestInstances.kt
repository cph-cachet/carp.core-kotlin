@file:Suppress( "WildcardImport" )

package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.data.*
import dk.cachet.carp.common.application.data.input.*
import dk.cachet.carp.common.application.data.input.elements.*
import dk.cachet.carp.common.application.devices.*
import dk.cachet.carp.common.application.sampling.*
import dk.cachet.carp.common.application.tasks.*
import dk.cachet.carp.common.application.triggers.*
import dk.cachet.carp.common.application.users.*
import kotlin.time.Duration


/**
 * An instance for each of the extending types from base classes in [dk.cachet.carp.common].
 */
val commonInstances = listOf(
    // `data` namespace.
    Acceleration( 42.0, 42.0, 42.0 ),
    CompletedTask( "Task", null ),
    ECG( 42.0 ),
    FreeFormText( "Some text" ),
    Geolocation( 42.0, 42.0 ),
    HeartRate( 60 ),
    NoData,
    RRInterval,
    SensorSkinContact( true ),
    SignalStrength( 0 ),
    StepCount( 42 ),
    TriggeredTask( 1, "Some task", "Destination device", TaskControl.Control.Start ),

    // `data.input` namespace.
    CustomInput( "42" ),
    Sex.Male,

    // `data.input.elements` namespace.
    SelectOne( "Sex", setOf( "Male", "Female" ) ),
    Text( "Name" ),

    // Devices in `devices` namespace.
    AltBeacon( "Kitchen" ),
    AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0, 0 ),
    BLEHeartRateDevice( "Polar" ),
    BLESerialNumberDeviceRegistration( "123456789" ),
    CustomProtocolDevice( "User's phone" ),
    Smartphone( "User's phone" ),

    // Shared device registrations in `devices` namespace.
    DefaultDeviceRegistration( "Some device" ),
    MACAddressDeviceRegistration( MACAddress( "00-00-00-00-00-00" ) ),

    // `sampling` namespace.
    BatteryAwareSamplingConfiguration(
        GranularitySamplingConfiguration( Granularity.Balanced ),
        GranularitySamplingConfiguration( Granularity.Coarse ),
    ),
    GranularitySamplingConfiguration( Granularity.Balanced ),
    IntervalSamplingConfiguration( Duration.milliseconds( 1000 ) ),
    NoOptionsSamplingConfiguration,

    // `tasks` namespace.
    BackgroundTask( "Start measures", listOf() ),
    CustomProtocolTask(
        "Custom study runtime",
        "{ \"\$type\": \"Study\", \"custom\": \"protocol\" }"
    ),
    WebTask( "Survey", emptyList(), "Some survey", "http://survey.com" ),

    // `triggers` namespace.
    ElapsedTimeTrigger( Smartphone( "User's phone" ), Duration.ZERO ),
    ManualTrigger(
        "User's phone",
        "Mood",
        "Describe how you are feeling at the moment."
    ),
    ScheduledTrigger(
        Smartphone( "User's phone"),
        TimeOfDay( 12 ), RecurrenceRule( RecurrenceRule.Frequency.DAILY )
    ),

    // `users` namespace.
    EmailAccountIdentity( "test@test.com" ),
    UsernameAccountIdentity( "Some user" ),
)
