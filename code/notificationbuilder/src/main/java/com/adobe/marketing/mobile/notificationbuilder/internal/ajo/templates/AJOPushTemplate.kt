/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.notificationbuilder.internal.ajo.templates

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.adobe.marketing.mobile.notificationbuilder.NotificationPriority
import com.adobe.marketing.mobile.notificationbuilder.NotificationVisibility
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants.ActionType
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants.AJOTemplatePropertyKeys
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants.PushPayloadKeys
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateType
import com.adobe.marketing.mobile.notificationbuilder.internal.util.IntentData
import com.adobe.marketing.mobile.notificationbuilder.internal.util.NotificationData
import com.adobe.marketing.mobile.services.Log
import org.json.JSONException
import org.json.JSONObject

/**
 * Base class for all Adobe Journey Optimizer (AJO) push templates.
 *
 * Parsing strategy (two zones):
 *  - [adb_template_properties] blob → title, body, imageUrl, largeIcon (template-specific fields)
 *  - Flat FCM data keys → sound, channelId, priority, visibility, actionType, actionUri,
 *    badgeCount, sticky (general notification config — backward-compat with old campaigns)
 */
internal sealed class AJOPushTemplate(val data: NotificationData) {

    private val SELF_TAG = "AJOPushTemplate"

    // Required — sourced from adb_template_properties blob
    internal val title: String

    // Required — sourced from adb_template_properties blob
    internal val body: String

    // Optional — sourced from adb_template_properties blob
    internal val imageUrl: String?

    // Optional — sourced from adb_template_properties blob
    internal val largeIcon: String?

    // Optional — version of the template schema, sourced from blob. Defaults to "1".
    internal val payloadVersion: String

    // --- Fields below are sourced from flat FCM data keys (backward-compat zone) ---

    // Optional, small icon resource name. Reads adb_small_icon then falls back to adb_icon.
    internal val smallIcon: String?

    // Optional, sound to play when the notification is shown
    internal val sound: String?

    // Optional, number to show on the badge of the app
    internal val badgeCount: Int

    // Optional, priority of the notification
    internal val priority: NotificationPriority

    // Optional, visibility of the notification
    internal val visibility: NotificationVisibility

    // Optional, notification channel (Android O+)
    internal val channelId: String?

    // Optional, action type for the notification tap
    internal val actionType: ActionType?

    // Optional, action uri for the notification tap
    internal val actionUri: String?

    // Optional, ticker text for accessibility services
    internal val ticker: String?

    // Optional, the type of push template this payload contains
    internal val templateType: PushTemplateType?

    // Optional, when true the notification persists after the user taps it
    internal val isNotificationSticky: Boolean

    // Flag to denote if the PushTemplate was built from an intent
    internal val isFromIntent: Boolean

    init {
        templateType = PushTemplateType.fromString(data.getString(PushPayloadKeys.TEMPLATE_TYPE))
        isFromIntent = data is IntentData

        // Parse the adb_template_properties JSON blob
        val props: JSONObject? = parseTemplateProperties(data.getString(PushPayloadKeys.AJO_TEMPLATE_PROPERTIES))

        // Required fields from blob
        title = props?.optJSONObject(AJOTemplatePropertyKeys.TITLE)
            ?.optString(AJOTemplatePropertyKeys.SubKeys.TEXT)
            ?.takeIf { it.isNotEmpty() }
            ?: data.getRequiredString(PushPayloadKeys.TITLE)

        body = props?.optJSONObject(AJOTemplatePropertyKeys.BODY)
            ?.optString(AJOTemplatePropertyKeys.SubKeys.TEXT)
            ?.takeIf { it.isNotEmpty() }
            ?: data.getRequiredString(PushPayloadKeys.BODY)

        // Optional fields from blob
        payloadVersion = props?.optString(AJOTemplatePropertyKeys.VERSION)
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_PAYLOAD_VERSION

        imageUrl = props?.optJSONObject(AJOTemplatePropertyKeys.IMAGE)
            ?.optString(AJOTemplatePropertyKeys.SubKeys.URL)
            ?.takeIf { it.isNotEmpty() }
            ?: data.getString(PushPayloadKeys.IMAGE_URL)

        largeIcon = props?.optJSONObject(AJOTemplatePropertyKeys.LARGE_ICON)
            ?.optString(AJOTemplatePropertyKeys.SubKeys.URL)
            ?.takeIf { it.isNotEmpty() }

        // Flat FCM keys — general notification config (backward-compat zone)
        smallIcon = data.getString(PushPayloadKeys.SMALL_ICON)
            ?: data.getString(PushPayloadKeys.LEGACY_SMALL_ICON)
        sound = data.getString(PushPayloadKeys.SOUND)
        channelId = data.getString(PushPayloadKeys.CHANNEL_ID)
        badgeCount = data.getInteger(PushPayloadKeys.BADGE_COUNT) ?: 0
        isNotificationSticky = data.getBoolean(PushPayloadKeys.STICKY) ?: false
        ticker = data.getString(PushPayloadKeys.TICKER)
        priority = NotificationPriority.fromString(data.getString(PushPayloadKeys.PRIORITY))
        visibility = NotificationVisibility.fromString(data.getString(PushPayloadKeys.VISIBILITY))
        actionUri = data.getString(PushPayloadKeys.ACTION_URI)
        actionType = ActionType.valueOf(
            data.getString(PushPayloadKeys.ACTION_TYPE) ?: ActionType.NONE.name
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getNotificationImportance(): Int =
        notificationImportanceMap[priority.stringValue] ?: NotificationManager.IMPORTANCE_DEFAULT

    private fun parseTemplateProperties(raw: String?): JSONObject? {
        if (raw.isNullOrEmpty()) return null
        return try {
            JSONObject(raw)
        } catch (e: JSONException) {
            Log.warning(
                "AJOPushTemplate",
                SELF_TAG,
                "Failed to parse adb_template_properties: ${e.localizedMessage}"
            )
            null
        }
    }

    companion object {
        private const val DEFAULT_PAYLOAD_VERSION = "1"

        @RequiresApi(api = Build.VERSION_CODES.N)
        internal val notificationImportanceMap: Map<String, Int> = mapOf(
            NotificationPriority.PRIORITY_MIN.toString() to NotificationManager.IMPORTANCE_MIN,
            NotificationPriority.PRIORITY_LOW.toString() to NotificationManager.IMPORTANCE_LOW,
            NotificationPriority.PRIORITY_DEFAULT.toString() to NotificationManager.IMPORTANCE_DEFAULT,
            NotificationPriority.PRIORITY_HIGH.toString() to NotificationManager.IMPORTANCE_HIGH,
            NotificationPriority.PRIORITY_MAX.toString() to NotificationManager.IMPORTANCE_MAX
        )
    }
}
