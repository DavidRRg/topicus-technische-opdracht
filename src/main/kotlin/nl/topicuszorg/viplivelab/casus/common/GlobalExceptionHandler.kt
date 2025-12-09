package nl.topicuszorg.viplivelab.casus.common.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import nl.topicuszorg.viplivelab.casus.appointment.domain.exception.*
import nl.topicuszorg.viplivelab.casus.common.ErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(InvalidAppointmentTimeException::class)
    fun handleInvalidTime(ex: InvalidAppointmentTimeException): ResponseEntity<ErrorResponse> {
        log.warn("Invalid appointment time: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("INVALID_APPOINTMENT_TIME", ex.message ?: "Invalid time range"))
    }

    @ExceptionHandler(AppointmentConflictException::class)
    fun handleConflict(ex: AppointmentConflictException): ResponseEntity<ErrorResponse> {
        log.info("Appointment conflict: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse("APPOINTMENT_CONFLICT", ex.message ?: "Appointment overlaps with existing one"))
    }

    @ExceptionHandler(NoFreeSlotAvailableException::class)
    fun handleNoSlot(ex: NoFreeSlotAvailableException): ResponseEntity<ErrorResponse> {
        log.info("No free slot available: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("NO_FREE_SLOT", ex.message ?: "No free slot available"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error while handling request", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("INTERNAL_ERROR", "Unexpected error"))
    }
}
