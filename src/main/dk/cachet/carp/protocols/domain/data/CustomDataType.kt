package dk.cachet.carp.protocols.domain.data

import com.beust.klaxon.*
import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicWrapper


/**
 * A wrapper used to load extending types from [DataType]s serialized as JSON which are unknown at runtime.
 */
data class CustomDataType( override val className: String, override val jsonSource: String ) : DataType(), UnknownPolymorphicWrapper
{
    override val category: DataCategory

    init
    {
        val json = Parser().parse( StringBuilder( jsonSource ) ) as JsonObject

        val categoryField = DataType::category.name
        val categoryString = json.string( categoryField )
            ?: throw IllegalArgumentException( "No '$categoryField' defined." )
        category = DataCategory.valueOf( categoryString )
    }
}