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
public enum MhdEnum {

    EXECUTIVE(R.string.executive),
    ADDRESS_TELEPHONE(R.string.address_telephone),
    WORKING_DAYS_HOURS(R.string.working_days),
    WEBSITE(R.string.website),
    LISTING_IN_SPYUR(R.string.listing_in_spyur);

    @StringRes private int mdh;

    MhdEnum(@StringRes int mdh) {
        this.mdh = mdh;
    }

    @Nullable
    public static MhdEnum fromString(Context context, String currentMhd) {
        for (MhdEnum mhd : MhdEnum.values()) {
            if (context.getString(mhd.mdh).equals(currentMhd))
                return mhd;
        }
        return null;
    }
}
