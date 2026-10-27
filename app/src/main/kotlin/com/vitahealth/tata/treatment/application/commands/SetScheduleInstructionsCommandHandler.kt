package com.vitahealth.tata.treatment.application.commands

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.valueobjects.AdministrationInstructions
import com.vitahealth.tata.treatment.domain.model.valueobjects.AdministrationSchedule
import com.vitahealth.tata.treatment.domain.model.valueobjects.ScheduleInstructions
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/** Validates explicit local administration times and their optional instructions. */
class SetScheduleInstructionsCommandHandler {
    private val timeFormatter =
        DateTimeFormatter.ofPattern("H:mm", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT)

    operator fun invoke(command: SetScheduleInstructionsCommand): AppResult<ScheduleInstructions> {
        val tokens = command.scheduleText.split(",").map(String::trim).filter(String::isNotBlank)

        if (tokens.isEmpty()) {
            return AppResult.Failure(
                message = "Agrega al menos un horario.",
                code = "EMPTY_SCHEDULE",
            )
        }

        val times =
            try {
                tokens.map { LocalTime.parse(it, timeFormatter) }
            } catch (_: DateTimeParseException) {
                return AppResult.Failure(
                    message = "Usa horarios con formato HH:mm, por ejemplo 08:00.",
                    code = "INVALID_SCHEDULE_TIME",
                )
            }

        val schedule =
            AdministrationSchedule.create(times)
                ?: return AppResult.Failure(
                    message = "Agrega al menos un horario.",
                    code = "EMPTY_SCHEDULE",
                )
        val instructions =
            AdministrationInstructions.parse(command.instructions)
                ?: return AppResult.Failure(
                    message = "Las indicaciones no pueden superar 500 caracteres.",
                    code = "INSTRUCTIONS_TOO_LONG",
                )

        return AppResult.Success(
            ScheduleInstructions(schedule = schedule, instructions = instructions)
        )
    }
}
