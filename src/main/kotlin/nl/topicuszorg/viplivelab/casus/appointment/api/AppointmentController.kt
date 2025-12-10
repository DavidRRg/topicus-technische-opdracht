package nl.topicuszorg.viplivelab.casus.appointment.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import nl.topicuszorg.viplivelab.casus.appointment.api.dto.*
import nl.topicuszorg.viplivelab.casus.appointment.domain.service.AppointmentService
import nl.topicuszorg.viplivelab.casus.common.ErrorResponse
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "Operations for creating and querying appointments.")
class AppointmentController(
    private val service: AppointmentService
) {

    @PostMapping
    @Operation(
        summary = "Create a new appointment",
        description = "Creates an appointment if the time range is valid and no overlap occurs.",
        responses = [
            ApiResponse(responseCode = "200", description = "Appointment created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "409", description = "Time conflict", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    fun create(@RequestBody request: CreateAppointmentRequest): AppointmentResponse {
        val appointment = service.createAppointment(request.start, request.end, request.description)
        return AppointmentResponse(
            id = appointment.id,
            start = appointment.start,
            end = appointment.end,
            description = appointment.description
        )
    }

    @GetMapping("/next-free-slot")
    @Operation(
        summary = "Find the next free slot",
        description = "Returns the earliest available time slot for a given duration.",
        responses = [
            ApiResponse(responseCode = "200", description = "Next free slot found"),
            ApiResponse(responseCode = "404", description = "No slot available", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    fun nextFreeSlot(
        @RequestParam("from") from: String,
        @RequestParam("durationMinutes") durationMinutes: Long,
        @RequestParam("searchUntil", required = false) searchUntil: String?
    ): NextFreeSlotResponse {
        val slot = service.findNextFreeSlot(
            from = ZonedDateTime.parse(from),
            duration = Duration.ofMinutes(durationMinutes),
            searchUntil = searchUntil?.let { ZonedDateTime.parse(it) }
        )
        return NextFreeSlotResponse(start = slot)
    }
}
