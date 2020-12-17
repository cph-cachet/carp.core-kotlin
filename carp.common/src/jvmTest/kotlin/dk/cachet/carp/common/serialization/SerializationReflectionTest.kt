package dk.cachet.carp.common.serialization

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.CUSTOM_INPUT_TYPE_NAME
import dk.cachet.carp.common.data.input.CustomInputSerializer
import dk.cachet.carp.common.data.input.InputElement
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.test.serialization.verifyTypesAreRegistered
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import org.reflections.Reflections
import java.lang.reflect.ParameterizedType
import kotlin.test.*


@ExperimentalSerializationApi
class SerializationReflectionTest
{
    @Test
    fun all_AccountIdentity_types_registered_for_serialization() =
        verifyTypesAreRegistered<AccountIdentity>( COMMON_SERIAL_MODULE )

    @Test
    fun all_Data_types_registered_for_serialization() =
        verifyTypesAreRegistered<Data>( COMMON_SERIAL_MODULE )

    @Test
    fun all_InputElement_types_registered_for_serialization() =
        verifyTypesAreRegistered<InputElement<*>>( COMMON_SERIAL_MODULE )

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
