package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.concreteAccountIdentityTypes
import dk.cachet.carp.common.application.concreteDataTypes
import dk.cachet.carp.common.application.concreteDeviceConfigurationTypes
import dk.cachet.carp.common.application.concreteDeviceRegistrationTypes
import dk.cachet.carp.common.application.concreteInputElementTypes
import dk.cachet.carp.common.application.concreteSamplingConfigurationTypes
import dk.cachet.carp.common.application.concreteTaskConfigurationTypes
import dk.cachet.carp.common.application.concreteTriggerTypes
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CUSTOM_INPUT_TYPE_NAME
import dk.cachet.carp.common.application.data.input.CustomInputSerializer
import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.test.serialization.getPolymorphicSerializers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import org.reflections.Reflections
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.test.*


@ExperimentalSerializationApi
class SerializationReflectionTest
{
    @Test
    fun all_Data_types_registered_for_serialization() =
        verifyTypesAreRegistered( concreteDataTypes )

    @Test
    fun all_InputElement_types_registered_for_serialization() =
        verifyTypesAreRegistered( concreteInputElementTypes )

    @Test
    fun all_DeviceConfiguration_types_registered_for_serialization() =
        verifyTypesAreRegistered( concreteDeviceConfigurationTypes )

    @Test
    fun all_DeviceRegistration_types_registered_for_serialization() =
        verifyTypesAreRegistered( concreteDeviceRegistrationTypes )

    @Test
    fun all_SamplingConfiguration_types_registered_for_serialization() =
        verifyTypesAreRegistered( concreteSamplingConfigurationTypes )

    @Test
    fun all_TaskConfiguration_types_registered_for_serialization() =
        verifyTypesAreRegistered( concreteTaskConfigurationTypes )

    @Test
    fun all_Trigger_types_registered_for_serialization() =
        verifyTypesAreRegistered( concreteTriggerTypes )

    @Test
    fun all_AccountIdentity_types_registered_for_serialization() =
        verifyTypesAreRegistered( concreteAccountIdentityTypes )

    private val polymorphicSerializers = getPolymorphicSerializers( COMMON_SERIAL_MODULE )

    /**
     * Verifies whether all [types] are registered for polymorphic serialization in [COMMON_SERIAL_MODULE].
     */
    private fun verifyTypesAreRegistered( types: List<KClass<out Any>> ) = types
        .filter { type ->
            // Wrappers for unknown types are only used at runtime and don't need to be serializable.
            type.java.interfaces.none { it == UnknownPolymorphicWrapper::class.java }
        }
        .forEach {
            val serializer = polymorphicSerializers[ it ]
            assertNotNull( serializer, "No serializer registered for '$it'." )
        }

    @InternalSerializationApi
    @Test
    fun all_InputElement_type_parameters_are_registered_in_CustomInputSerializer()
    {
        // Get registered data types for CustomInputSerializer.
        val customInputSerializer = COMMON_SERIAL_MODULE.getPolymorphic( Data::class, CUSTOM_INPUT_TYPE_NAME )
            as CustomInputSerializer
        val supportedDataTypes: Set<String> = customInputSerializer.dataTypeMap.keys

        // Get all type parameters of InputElement<T> implementations.
        val klass = InputElement::class
        val namespace = klass.java.`package`.name
        val reflections = Reflections( namespace )
        val usedDataTypes = reflections
            .getSubTypesOf( klass.java )
            .flatMap { it.genericInterfaces.toList() }
            .filterIsInstance<ParameterizedType>()
            .filter { it.rawType == InputElement::class.java }
            .map {
                val typeParameter = it.actualTypeArguments.single()
                // CustomInputSerializer currently does not register full type names.
                // Furthermore, for fully qualified names these will need to be mapped from Java classes to Kotlin classes.
                typeParameter.typeName.split( '.' ).last()
            }
            .toSet()

        assertTrue( supportedDataTypes.containsAll( usedDataTypes ) )
    }
}
