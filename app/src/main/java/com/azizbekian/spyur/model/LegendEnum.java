package com.azizbekian.spyur.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.azizbekian.spyur.R;

/**
 * Created on May 01, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public enum LegendEnum {

    CONTACT_INFORMATION(R.string.contact_information);

    @StringRes private int legend;

    LegendEnum(@StringRes int legend) {
        this.legend = legend;
    }

    @Nullable
    public static LegendEnum fromString(Context context, String currentLegend) {
        for (LegendEnum legend : LegendEnum.values()) {
            if (context.getString(legend.legend).equals(currentLegend))
                return legend;
        }
        return null;
    }
}
