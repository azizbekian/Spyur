package com.incentive.yellowpages.misc;

import android.support.annotation.StringDef;

import com.incentive.yellowpages.data.remote.ApiContract;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        ApiContract.AM, ApiContract.RU, ApiContract.EN
})
public @interface LanguageType {
}