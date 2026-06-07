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

import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants.AJOTemplatePropertyKeys
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants.PushPayloadKeys
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateType
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_BODY
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_FLAT_BODY
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_FLAT_TITLE
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_IMAGE_URL
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_LARGE_ICON_URL
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_TEMPLATE_PROPS_CENTER_CROP
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_TEMPLATE_PROPS_FIT_CENTER
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_TEMPLATE_PROPS_NO_SCALE
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AJO_MOCKED_TITLE
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.MOCKED_ACTION_BUTTON_DATA
import com.adobe.marketing.mobile.notificationbuilder.internal.util.MapData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AJOBasicPushTemplateTest {

    // ── AJOPushTemplate (base class) parsing ──────────────────────────────────

    @Test
    fun `AJOPushTemplate parses blob title and body over flat keys`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_FIT_CENTER
                )
            )
        )
        assertEquals(AJO_MOCKED_TITLE, template.title)
        assertEquals(AJO_MOCKED_BODY, template.body)
    }

    @Test
    fun `AJOPushTemplate falls back to flat title and body when blob is absent`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY
                )
            )
        )
        assertEquals(AJO_MOCKED_FLAT_TITLE, template.title)
        assertEquals(AJO_MOCKED_FLAT_BODY, template.body)
    }

    @Test
    fun `AJOPushTemplate falls back to flat keys when blob is malformed JSON`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to "not valid json {{{"
                )
            )
        )
        assertEquals(AJO_MOCKED_FLAT_TITLE, template.title)
        assertEquals(AJO_MOCKED_FLAT_BODY, template.body)
    }

    @Test
    fun `AJOPushTemplate parses imageUrl and largeIcon from blob`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_FIT_CENTER
                )
            )
        )
        assertEquals(AJO_MOCKED_IMAGE_URL, template.imageUrl)
        assertEquals(AJO_MOCKED_LARGE_ICON_URL, template.largeIcon)
    }

    @Test
    fun `AJOPushTemplate falls back to flat imageUrl when blob has no image`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.IMAGE_URL to "https://example.com/flat.jpg"
                )
            )
        )
        assertEquals("https://example.com/flat.jpg", template.imageUrl)
    }

    @Test
    fun `AJOPushTemplate reads flat notification properties`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.SOUND to "bells",
                    PushPayloadKeys.CHANNEL_ID to "ajo_channel",
                    PushPayloadKeys.PRIORITY to "PRIORITY_HIGH",
                    PushPayloadKeys.VISIBILITY to "PUBLIC",
                    PushPayloadKeys.BADGE_COUNT to "3",
                    PushPayloadKeys.STICKY to "true",
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_FIT_CENTER
                )
            )
        )
        assertEquals("bells", template.sound)
        assertEquals("ajo_channel", template.channelId)
        assertEquals(3, template.badgeCount)
        assertEquals(true, template.isNotificationSticky)
    }

    // ── AJOBasicPushTemplate specific ─────────────────────────────────────────

    @Test
    fun `AJOBasicPushTemplate sets imgScaleType to fit_center from blob`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_FIT_CENTER
                )
            )
        )
        assertEquals(AJOTemplatePropertyKeys.ScaleType.FIT_CENTER, template.imgScaleType)
        assertEquals(AJOTemplatePropertyKeys.ScaleType.FIT_CENTER, template.largeIconScaleType)
    }

    @Test
    fun `AJOBasicPushTemplate sets imgScaleType to center_crop from blob`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_CENTER_CROP
                )
            )
        )
        assertEquals(AJOTemplatePropertyKeys.ScaleType.CENTER_CROP, template.imgScaleType)
        assertEquals(AJOTemplatePropertyKeys.ScaleType.CENTER_CROP, template.largeIconScaleType)
    }

    @Test
    fun `AJOBasicPushTemplate defaults imgScaleType to center_crop when scale_type key is absent`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_NO_SCALE
                )
            )
        )
        assertEquals(AJOTemplatePropertyKeys.ScaleType.CENTER_CROP, template.imgScaleType)
        assertEquals(AJOTemplatePropertyKeys.ScaleType.CENTER_CROP, template.largeIconScaleType)
    }

    @Test
    fun `AJOBasicPushTemplate parses action buttons from flat adb_act key`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.ACTION_BUTTONS to MOCKED_ACTION_BUTTON_DATA,
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_FIT_CENTER
                )
            )
        )
        assertEquals(2, template.actionButtonsList?.size)
        assertEquals("Go to chess.com", template.actionButtonsList?.get(0)?.label)
        assertEquals("Open the app", template.actionButtonsList?.get(1)?.label)
    }

    @Test
    fun `AJOBasicPushTemplate returns null action buttons when adb_act is absent`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_FIT_CENTER
                )
            )
        )
        assertNull(template.actionButtonsList)
    }

    @Test
    fun `AJOBasicPushTemplate returns null action buttons when adb_act is invalid JSON`() {
        val template = AJOBasicPushTemplate(
            MapData(
                mutableMapOf(
                    PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.AJO_BASIC.value,
                    PushPayloadKeys.TITLE to AJO_MOCKED_FLAT_TITLE,
                    PushPayloadKeys.BODY to AJO_MOCKED_FLAT_BODY,
                    PushPayloadKeys.ACTION_BUTTONS to "not json",
                    PushPayloadKeys.AJO_TEMPLATE_PROPERTIES to AJO_MOCKED_TEMPLATE_PROPS_FIT_CENTER
                )
            )
        )
        assertNull(template.actionButtonsList)
    }
}
