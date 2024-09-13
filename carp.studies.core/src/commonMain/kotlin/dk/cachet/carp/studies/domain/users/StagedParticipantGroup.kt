package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.participantIds
import kotlinx.serialization.*


/**
 * A group of participants configured during recruitment,
 * intended to be deployed as a whole once configuration is completed.
 */
@Serializable
data class StagedParticipantGroup(
    /**
     * The identifier for this participant group, used as deployment ID once the participant group is deployed.
     */
    val id: UUID = UUID.randomUUID()
)
{
    private val _participantIds: MutableSet<AssignedParticipantRoles> = mutableSetOf()
    val participantIds: Set<UUID>
        get() = _participantIds.participantIds()

    val roleAssignments: Set<AssignedParticipantRoles>
        get() = _participantIds
    /**
     * Determines whether this participant group has been deployed.
     */
    var isDeployed: Boolean = false
        private set



    /**
     * Add participants and their role assignments with [roleAssignments] to this group.
     * This is only allowed when the group hasn't been deployed yet.
     *
     * @throws IllegalStateException when this participant group is already deployed.
     */
    fun addParticipants( roleAssignments: Set<AssignedParticipantRoles> )
    {
        check( !isDeployed ) { "Can't add participant after a participant group has been deployed." }

        _participantIds.addAll( roleAssignments )
    }

    /**
     * Update participants in this group to [roleAssignments].
     * This is only allowed when the group hasn't been deployed yet.
     * Participants not in [roleAssignments] are removed from the group.
     *
     * @throws IllegalStateException when this participant group is already deployed.
     */
    fun updateParticipants( roleAssignments: Set<AssignedParticipantRoles> )
    {
        check( !isDeployed ) { "Can't update participants after a participant group has been deployed." }

        _participantIds.retainAll( roleAssignments )
        _participantIds.addAll( roleAssignments )
    }


    /**
     * Specify that a deployment for this participant group has been created.
     *
     * @throws IllegalStateException when no participants to deploy are specified.
     */
    fun markAsDeployed()
    {
        check( participantIds.isNotEmpty() ) { "No participants specified to deploy." }

        isDeployed = true
    }
}
