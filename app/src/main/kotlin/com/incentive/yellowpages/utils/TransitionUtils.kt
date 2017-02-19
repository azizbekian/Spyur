package com.incentive.yellowpages.utils

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.support.v4.util.Pair
import android.view.View
import java.util.*

class TransitionUtils private constructor() {

    init {
        throw RuntimeException("Unable to instantiate class " + javaClass.canonicalName)
    }

    companion object {

        /**
         * Create the transition participants required during a activity transition while
         * avoiding glitches with the system UI.

         * @param activity         The activity used as start for the transition.
         * *
         * @param includeStatusBar If false, the status bar will not be added as the transition
         * *                         participant.
         * *
         * @return All transition participants.
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun createSafeTransitionParticipants(activity: Activity,
                                             includeStatusBar: Boolean,
                                             vararg otherParticipants: Pair<View, String>?): Array<Pair<View, String>> {
            // Avoid system UI glitches as described here:
            // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
            val decor = activity.window.decorView
            var statusBar: View? = null
            if (includeStatusBar) {
                statusBar = decor.findViewById(android.R.id.statusBarBackground)
            }
            val navBar = decor.findViewById(android.R.id.navigationBarBackground)

            // Create pair of transition participants.
            val participants = mutableListOf<Pair<View, String>>()
            addNonNullViewToTransitionParticipants(statusBar, participants)
            addNonNullViewToTransitionParticipants(navBar, participants)
            // only add transition participants if there's at least one none-null element
            val participantsList: ArrayList<Pair<View, String>> = arrayListOf()
            for (p in otherParticipants) {
                if (null != p) participantsList.add(p)
            }
            if (!(participantsList.size == 1 && otherParticipants[0] == null)) {
                participants.addAll(participantsList)
            }

            return participants.toTypedArray()
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun addNonNullViewToTransitionParticipants(view: View?, participants: MutableList<Pair<View, String>>) {
            if (view == null) return
            participants.add(Pair(view, view.transitionName))
        }

    }

}
