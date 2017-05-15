/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.incentive.yellowpages.utils

import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.incentive.yellowpages.R

abstract class PermissionUtils private constructor() {

    init {
        throw RuntimeException("Unable to instantiate class " + javaClass.canonicalName)
    }

    /**
     * A dialog that displays a permission denied message.
     */
    class PermissionDeniedDialog : DialogFragment() {

        private var mFinishActivity = false

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            mFinishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY)

            return AlertDialog.Builder(activity)
                    .setMessage(R.string.permission_location_denied)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
        }

        override fun onDismiss(dialog: DialogInterface?) {
            super.onDismiss(dialog)
            if (mFinishActivity) {
                activity.finish()
            }
        }

        companion object {

            private val ARGUMENT_FINISH_ACTIVITY = "finish"

            /**
             * Creates a new instance of this dialog and optionally finishes the calling Activity
             * when the 'Ok' button is clicked.
             */
            fun newInstance(finishActivity: Boolean): PermissionDeniedDialog {
                val arguments = Bundle()
                arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)

                val dialog = PermissionDeniedDialog()
                dialog.arguments = arguments
                return dialog
            }
        }
    }

    companion object {

        fun requestPermissions(activity: AppCompatActivity, requestId: Int,
                              permissions: Array<String>) {
            ActivityCompat.requestPermissions(activity, permissions, requestId)
        }

        /**
         * Checks for granted permissions.
         *
         * @param context     The context, where the permission is being checked from.
         * @param permissions An array of permission strings, that are required to be granted.
         * @return If all the permissions from `permissions` are granted - true. Otherwise - false.
         */
        @TargetApi(Build.VERSION_CODES.M)
        fun isPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
            return permissions.none { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
        }

        fun isPermissionsGranted(grantResults: IntArray): Boolean {
            return grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }

    }

}
