package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.EnumObjectList


/**
 * A helper class to construct iterable objects which hold [DataTypeMetaData] member definitions.
 * This is similar to an enum, but removes the need for an intermediate enum type and generic type parameters are retained per member.
 *
 * Extend from this class as an object and assign members as follows: `val SOME_TYPE = add( "dk.cachet.carp.sometype" )`.
 */
open class DataTypeMetaDataList : EnumObjectList<DataTypeMetaData>()
{
    fun add( fullyQualifiedName: String ): DataTypeMetaData =
        DataTypeMetaData( DataType.fromString( fullyQualifiedName ) )
}
