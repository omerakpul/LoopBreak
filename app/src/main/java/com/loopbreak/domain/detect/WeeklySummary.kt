package com.loopbreak.domain.detect

import com.loopbreak.domain.model.LoopEvent
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/** How a week of loops went compared with the week before. Pure Kotlin. */
data class WeeklySummary(
    val total: Int,
    val previousTotal: Int,
    /** The app with the highest score this week, or null when there were no loops. */
    val topPackage: String?,
    val topScore: Int,
) {
    val delta: DayDelta? get() = DayDelta.of(total, previousTotal)

    /** Nothing to report: no loops this week and none the week before. */
    val isEmpty: Boolean get() = total == 0 && previousTotal == 0

    companion object {
        fun of(thisWeek: List<LoopEvent>, lastWeek: List<LoopEvent>): WeeklySummary {
            val perApp = thisWeek
                .groupBy { it.packageName }
                .mapValues { LoopScore.of(it.value) }
                .filterValues { it > 0 }
            val top = perApp.maxByOrNull { it.value }
            return WeeklySummary(
                total = LoopScore.of(thisWeek),
                previousTotal = LoopScore.of(lastWeek),
                topPackage = top?.key,
                topScore = top?.value ?: 0,
            )
        }
    }
}

/** When the weekly summary goes out: Sunday evening, local time. */
object WeeklySchedule {
    private const val SEND_HOUR = 19

    /** Milliseconds from [now] until the next Sunday 19:00 (a full week if it is Sunday 19:00 or later). */
    fun delayUntilNext(now: ZonedDateTime): Long {
        var next = now
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .withHour(SEND_HOUR).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusWeeks(1)
        return Duration.between(now, next).toMillis()
    }
}
