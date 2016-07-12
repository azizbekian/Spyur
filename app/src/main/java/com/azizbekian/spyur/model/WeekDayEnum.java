package com.azizbekian.spyur.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.azizbekian.spyur.R;

/**
 * Created on May 02, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public enum WeekDayEnum {

    MON(R.string.weekday_mon),
    TUE(R.string.weekday_tue),
    WED(R.string.weekday_wed),
    THU(R.string.weekday_thu),
    FRI(R.string.weekday_fri),
    SAT(R.string.weekday_sat),
    SUN(R.string.weekday_sun);

    @StringRes private int weekDay;

    WeekDayEnum(@StringRes int weekDay) {
        this.weekDay = weekDay;
    }

    @Nullable
    public static WeekDayEnum fromString(Context context, String currentWeekDay) {
        for (WeekDayEnum weekDay : WeekDayEnum.values()) {
            if (context.getString(weekDay.weekDay).equals(currentWeekDay))
                return weekDay;
        }
        return null;
    }

}
