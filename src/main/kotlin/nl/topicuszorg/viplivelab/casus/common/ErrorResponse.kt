package nl.topicuszorg.viplivelab.casus.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Standardized error response.")
data class ErrorResponse(

    @Schema(description = "Machine-readable error code.", example = "APPOINTMENT_CONFLICT")
    val code: String,

    @Schema(description = "Human-readable error message.", example = "Appointment overlaps with an existing one.")
    val message: String
)