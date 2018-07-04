# Study Protocol Domain Model and Application Services
A domain model and application services to specify study protocols which can be deployed to one or more stationary or mobile devices. This project is part of the [CACHET Research Platform (CARP)](https://github.com/cph-cachet/carp.documentation).

Using this library, CARP-compatible studies can be defined:
```
// Create a new study protocol.
val owner = ProtocolOwner()
val protocol = StudyProtocol( owner, "Example study" )

// Define which devices are used for data collection.
val phone = Smartphone( "Patient phone" )
protocol.addMasterDevice( phone )

// Define what needs to be measured, on which device, when.
val measures = listOf( GpsMeasure(), StepCountMeasure() )
val startMeasures = IndefiniteTask( "Start measures", measures )
protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

// JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
val json = protocol.getSnapshot().toJson()
```

Currently this project is under development and only an initial unstable alpha version is available. Many changes will happen as the rest of the infrastructure is implemented. Once a minimum viable product is completed, a first version will be released and more documentation will be added. 

## Setting up using IntelliJ IDEA
- Install Gradle 4.8.1, e.g., [using Chocolatey on Windows](https://chocolatey.org/packages/gradle)
- Install the Kotlin plugin (1.2.50-release-IJ2018.1-1) for IntelliJ IDEA: `Tools->Kotlin->Configure Kotlin Plugin Updates`
- Install the [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization#working-in-intellij-idea) IDE [plugin (for Kotlin 1.2.50)](https://teamcity.jetbrains.com/viewLog.html?buildId=lastPinned&buildTypeId=KotlinTools_KotlinxSerialization_KotlinCompilerWithSerializationPlugin&tab=artifacts&guest=1)