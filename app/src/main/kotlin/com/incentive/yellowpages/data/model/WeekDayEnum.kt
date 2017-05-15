package com.incentive.yellowpages.data.model

import android.content.Context
import android.support.annotation.StringRes

import com.incentive.yellowpages.R

enum class WeekDayEnum constructor(@StringRes private val weekDay: Int) {

    MON(R.string.weekday_mon),
    TUE(R.string.weekday_tue),
    WED(R.string.weekday_wed),
    THU(R.string.weekday_thu),
    FRI(R.string.weekday_fri),
    SAT(R.string.weekday_sat),
    SUN(R.string.weekday_sun);

    companion object {

        fun fromString(context: Context, currentWeekDay: String): WeekDayEnum? {
            return WeekDayEnum.values().firstOrNull { context.getString(it.weekDay) == currentWeekDay }
        }
    }

}
