package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.domain.ExtractUniqueKeyMap
import dk.cachet.carp.protocols.application.users.ExpectedParticipantData


/**
 * An initially empty configuration to start defining expected participants and data to be input by users.
 */
@Suppress( "Immutable", "DataClass" )
class EmptyParticipantConfiguration : ParticipantConfiguration
{
    private val _expectedParticipantData: MutableSet<ExpectedParticipantData> = mutableSetOf()

    override val expectedParticipantData: Set<ExpectedParticipantData>
        get() = _expectedParticipantData.toSet()

    private val _participantRoles: ExtractUniqueKeyMap<String, ParticipantRole> =
        ExtractUniqueKeyMap( { role -> role.role } )
        {
            key -> IllegalArgumentException( "Role name \"$key\" is not unique within participant configuration." )
        }

    override val participantRoles: Set<ParticipantRole>
        get() = _participantRoles.values.toSet()


    override fun addExpectedParticipantData( expectedData: ExpectedParticipantData ): Boolean
    {
        val conflictingAttribute = _expectedParticipantData
            .map { it.attribute }
            .any { it.inputDataType == expectedData.inputDataType && it != expectedData.attribute }
        require( !conflictingAttribute )
        {
            "The input data type of the expected data to add matches that of previously-added expected data, " +
            "but the participant attributes are different."
        }

        return _expectedParticipantData.add( expectedData )
    }

    override fun removeExpectedParticipantData( expectedData: ExpectedParticipantData ): Boolean =
        _expectedParticipantData.remove( expectedData )

    override fun addParticipantRole( role: ParticipantRole ): Boolean =
        _participantRoles.tryAddIfKeyIsNew( role )

    override fun removeParticipantRole( role: ParticipantRole ): Boolean =
        _participantRoles.remove( role )
}
