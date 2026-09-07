package com.uliana.myplanner.domain

import com.uliana.myplanner.data.RepeatRule
import com.uliana.myplanner.data.ScenarioWithSteps
import com.uliana.myplanner.data.TaskEntity
import java.time.LocalDateTime
import java.util.UUID

object ScenarioEngine {

    fun instantiate(scenario: ScenarioWithSteps, startAt: LocalDateTime): List<TaskEntity> {
        val runId = UUID.randomUUID().toString()
        return scenario.steps.sortedBy { it.stepOrder }.map { step ->
            val stepStart = startAt.plusMinutes(step.offsetMinutesFromStart.toLong())
            val stepEnd = stepStart.plusMinutes(step.durationMinutes.toLong())
            TaskEntity(
                title = step.title,
                categoryId = scenario.scenario.categoryId,
                anchorStart = stepStart,
                anchorEnd = stepEnd,
                durationMinutes = step.durationMinutes.toLong(),
                repeatRule = RepeatRule(),
                reminderMinutesBefore = step.reminderMinutesBefore,
                scenarioRunId = runId,
                scenarioStepOrder = step.stepOrder
            )
        }
    }
}
