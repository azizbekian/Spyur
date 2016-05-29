package com.azizbekian.spyur.misc;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.azizbekian.spyur.rest.SpyurApi;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        SpyurApi.AM, SpyurApi.RU, SpyurApi.EN
})
public @interface LanguageType {
}