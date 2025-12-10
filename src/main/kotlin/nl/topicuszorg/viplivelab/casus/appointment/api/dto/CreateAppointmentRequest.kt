package nl.topicuszorg.viplivelab.casus.appointment.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Request payload for creating a new appointment.")
data class CreateAppointmentRequest(

    @Schema(
        description = "Start of the appointment in ISO-8601 format with timezone.",
        example = "2025-01-10T09:00:00+01:00[Europe/Amsterdam]"
    )
    val start: ZonedDateTime,

    @Schema(
        description = "End of the appointment in ISO-8601 format with timezone.",
        example = "2025-01-10T09:30:00+01:00[Europe/Amsterdam]"
    )
    val end: ZonedDateTime,

    @Schema(
        description = "Optional description of the appointment.",
        example = "Intake gesprek"
    )
    val description: String? = null
)