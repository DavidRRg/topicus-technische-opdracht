package nl.topicuszorg.viplivelab.casus.appointment.domain.model

import java.time.ZonedDateTime
import java.util.UUID

/**
 * Domain model representing a single appointment in the agenda.
 *
 * Domain invariants (enforced in the service layer, not here):
 * - start must be strictly before end
 * - appointments are not allowed to overlap
 *
 * The description is optional metadata and does not affect the business rules.
 */

data class Appointment(
    val id: UUID,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val description: String? = null
)