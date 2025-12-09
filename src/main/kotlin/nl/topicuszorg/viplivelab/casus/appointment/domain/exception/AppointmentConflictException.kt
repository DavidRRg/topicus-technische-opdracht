package nl.topicuszorg.viplivelab.casus.appointment.domain.exception

/**
 * Thrown when a new appointment would overlap with an existing one.
 */
class AppointmentConflictException(message: String) : RuntimeException(message)
