package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlin.reflect.*


/**
 * The serialization description for [PolymorphicSerializer].
 */
object PolymorphicSerializerClassDesc : SerialClassDescImpl( "kotlin.Any" )
{
    override val kind: KSerialClassKind = KSerialClassKind.POLYMORPHIC

    init
    {
        addElement( "klass" )
        addElement( "value" )
    }
}


/**
 * A serializer which can (de)serialize registered polymorph types by including type information in the serialized representation (supporting multiplatform).
 *
 * This is a custom version of the PolymorphicSerializer included in `kotlinx.serialization` which relies on types being registered manually.
 * This allows this serializer to also be used when targeting a JavaScript runtime (the normal PolymorphicSerializer is only supported on JVM).
 *
 * The downsides are (due to the fact that the JavaScript runtime does not have access to fully qualified class names):
 * (1) all types that need to be serialized need to be registered by passing their fully qualified name manually.
 * (2) no types with the same name can be registered.
 */
object PolymorphicSerializer : KSerializer<Any>
{
    override val serialClassDesc: KSerialClassDesc = PolymorphicSerializerClassDesc

    private val simpleNameSerializers = mutableMapOf<String, KSerializer<Any>>()
    private val qualifiedSerializers = mutableMapOf<String, KSerializer<Any>>()


    fun <T: Any> registerSerializer( klass: KClass<T>, qualifiedName: String )
    {
        val className = klass.simpleName!! // TODO: Is this dangerous?
        val serializer = klass.serializer() as KSerializer<Any> // TODO: Is this dangerous?

        // TODO: Throw exception when type with the same name is already registered.
        simpleNameSerializers[ className ] = serializer
        qualifiedSerializers[ qualifiedName ] = serializer
    }

    private fun getSerializerBySimpleClassName( className: String ): KSerializer<Any>
    {
        if ( !simpleNameSerializers.containsKey( className ) )
        {
            throw NoSuchElementException( "No polymorphic serializer is registered for the class '$className'." )
        }

        return simpleNameSerializers[ className ]!!
    }

    private fun getSerializerByQualifiedName( qualifiedName: String ): KSerializer<Any>
    {
        if ( !qualifiedSerializers.containsKey( qualifiedName ) )
        {
            throw NoSuchElementException( "No polymorphic serializer is registered with the qualified name '$qualifiedName'." )
        }

        return qualifiedSerializers[ qualifiedName ]!!
    }

    override fun save( output: KOutput, obj: Any )
    {
        val saver = getSerializerBySimpleClassName( obj::class.simpleName!! )

        @Suppress( "NAME_SHADOWING" )
        val output = output.writeBegin( serialClassDesc )
        output.writeStringElementValue( serialClassDesc, 0, saver.serialClassDesc.name )
        output.writeSerializableElementValue( serialClassDesc, 1, saver, obj )
        output.writeEnd( serialClassDesc )
    }

    override fun load( input: KInput ): Any
    {
        @Suppress( "NAME_SHADOWING" )
        val input = input.readBegin( serialClassDesc )
        var klassName: String? = null
        var value: Any? = null
        mainLoop@ while ( true )
        {
            when ( input.readElement( serialClassDesc ) )
            {
                KInput.READ_ALL ->
                {
                    klassName = input.readStringElementValue( serialClassDesc, 0 )
                    val loader = getSerializerByQualifiedName( klassName )
                    value = input.readSerializableElementValue( serialClassDesc, 1, loader )
                    break@mainLoop
                }
                KInput.READ_DONE ->
                {
                    break@mainLoop
                }
                0 ->
                {
                    klassName = input.readStringElementValue( serialClassDesc, 0 )
                }
                1 ->
                {
                    klassName = requireNotNull( klassName ) { "Cannot read polymorphic value before its type token" }
                    val loader = getSerializerByQualifiedName( klassName )
                    value = input.readSerializableElementValue( serialClassDesc, 1, loader )
                }
                else -> throw SerializationException( "Invalid index" )
            }
        }

        input.readEnd( serialClassDesc )
        return requireNotNull( value ) { "Polymorphic value have not been read" }
    }
}


/**
 * A serializer for polymorphic [List]'s relying on the [PolymorphicSerializer] (supporting multiplatform).
 */
object PolymorphicArrayListSerializer : KSerializer<List<Any>> by ArrayListSerializer( PolymorphicSerializer )