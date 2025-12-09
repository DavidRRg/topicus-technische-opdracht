package nl.topicuszorg.viplivelab.casus.appointment.domain.exception

/**
 * Thrown when an appointment has an invalid time range,
 * for example when the end is not after the start.
 */
class InvalidAppointmentTimeException(message: String) : RuntimeException(message)