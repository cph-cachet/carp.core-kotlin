package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicWrapper
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [DataType] serialized as JSON which are unknown at runtime.
 */
data class CustomDataType( override val className: String, override val jsonSource: String ) : DataType(), UnknownPolymorphicWrapper
{
    override val category: DataCategory

    init
    {
        val parser = JsonTreeParser( jsonSource )
        val json = parser.readFully() as JsonObject

        val categoryField = DataType::category.name
        if ( !json.containsKey( categoryField ) )
        {
            throw IllegalArgumentException( "No '$categoryField' defined." )
        }
        val categoryString = json[ categoryField ].content
        category = DataCategory.valueOf( categoryString )
    }
}