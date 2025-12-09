package nl.topicuszorg.viplivelab.casus.appointment.domain.exception

/**
 * Thrown when the system cannot find a free time slot
 * that fits the requested duration starting from a given moment.
 */
class NoFreeSlotAvailableException(message: String) : RuntimeException(message)