package com.incentive.yellowpages.data.model

import android.content.Context
import android.support.annotation.StringRes
import com.incentive.yellowpages.R

enum class MhdEnum constructor(@StringRes private val mdh: Int) {

    EXECUTIVE(R.string.executive),
    ADDRESS_TELEPHONE(R.string.address_telephone),
    WEBSITE(R.string.website),
    LISTING_IN_SPYUR(R.string.listing_in_spyur);

    companion object {

        fun fromString(context: Context, currentMhd: String): MhdEnum? {
            return values().firstOrNull { context.getString(it.mdh) == currentMhd }
        }
    }
}
