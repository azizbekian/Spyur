package com.incentive.yellowpages.data.model

import android.content.Context
import android.support.annotation.StringRes
import com.incentive.yellowpages.R

enum class LegendEnum constructor(@StringRes private val legend: Int) {

    CONTACT_INFORMATION(R.string.contact_information);

    companion object {

        fun fromString(context: Context, currentLegend: String): LegendEnum? {
            return LegendEnum.values().firstOrNull { context.getString(it.legend) == currentLegend }
        }
    }
}
