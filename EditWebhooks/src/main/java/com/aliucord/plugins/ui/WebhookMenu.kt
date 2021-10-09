package com.aliucord.plugins.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.widgets.LinearLayout
import com.aliucord.plugins.utils.Webhook
import com.discord.app.AppBottomSheet
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R
import android.widget.TextView
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.fragments.InputDialog
import com.aliucord.Constants
import androidx.core.content.res.ResourcesCompat
import com.aliucord.plugins.utils.WebhookRequest

import com.aliucord.Http
import com.google.gson.JsonObject
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.analytics.AnalyticSuperProperties
import com.aliucord.utils.ReflectUtils
import com.discord.stores.StoreStream
import androidx.fragment.app.FragmentManager

import com.discord.api.message.attachment.MessageAttachment
import com.discord.utilities.SnowflakeUtils
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemAttachment

import com.aliucord.plugins.ui.WebhookList

class WebhookMenu(
    private val webhook: Webhook,
    private val parent: WebhookList
) : AppBottomSheet() {
    
    override fun getContentViewResId() = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View {
        val context = inflater.context
        val layout = LinearLayout(context)
        layout.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))

        val deleteIcon = ContextCompat.getDrawable(context, R.d.ic_delete_24dp)
        val copyIcon = ContextCompat.getDrawable(context, R.d.ic_copy_24dp)
        val avatarIcon = ContextCompat.getDrawable(context, R.d.ic_profile_24dp)
        val renameIcon = ContextCompat.getDrawable(context, R.d.ic_edit_24dp)

        val title = TextView(context, null, 0, R.h.UiKit_Settings_Item_Header).apply {
            text = webhook.name
        }
        
        val deleteWebhook = TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
            text = "Delete"
            setCompoundDrawablesRelativeWithIntrinsicBounds(deleteIcon, null, null, null)
            setOnClickListener {
                val coDialog = ConfirmDialog()
                                .setTitle("Delete webhook")
                                .setIsDangerous(true)
                                .setDescription("Do you want to delete \"${webhook.name}\" webhook?")
                coDialog.setOnOkListener {
                    coDialog.dismiss()
                    Utils.threadPool.execute { deleteWebhook() }
            
                }
                coDialog.show(parentFragmentManager, "DeleteWebhook")
            }
            setClickable(true)
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
        }

        val copyWebhook = TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
            text = "Copy url"
            setCompoundDrawablesRelativeWithIntrinsicBounds(copyIcon, null, null, null)
            setOnClickListener {
                Utils.setClipboard("Webhook Url", webhook.url)
                Utils.showToast("Webhook url copied to clipboard")
                dismiss()
            }
            setClickable(true)
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
        }

        val viewAvatar = TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
            text = "View avatar"
            setCompoundDrawablesRelativeWithIntrinsicBounds(avatarIcon, null, null, null)
            setOnClickListener {
                viewAvatar()
            }
            setClickable(true)
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
        }

        val renameWebhook = TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
            text = "Rename"
            setCompoundDrawablesRelativeWithIntrinsicBounds(renameIcon, null, null, null)
            setOnClickListener {
                val inDialog = InputDialog()
                                .setTitle("Rename webhook")
                                .setDescription("Enter name of the name for ${webhook.name}")
                                .setPlaceholderText("Webhook Name")
                                inDialog.setOnOkListener {
                                    Utils.threadPool.execute {
                                        rename(inDialog.input.toString().trim())
                                    }
                                    inDialog.dismiss()
                                }
                        inDialog.show(parentFragmentManager, "RenameWebhook")
            }
            setClickable(true)
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
        }


        layout.addView(title)
        layout.addView(copyWebhook)
        layout.addView(renameWebhook)
        layout.addView(deleteWebhook)
        if (webhook.avatar != null)
            layout.addView(viewAvatar)

        return layout
    }

    private fun deleteWebhook() {
        Http.Request("https://discord.com/api/v9/webhooks/%s".format(webhook.id), "DELETE")
                .setHeader("Authorization", ReflectUtils.getField(StoreStream.getAuthentication(), "authToken") as String?)
                .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
                .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
                .setHeader("Accept", "*/*")
                .execute()
        Utils.showToast("Webhook deleted")
        parent.fetchList()
        dismiss()
    }

    private fun viewAvatar() {
        val msgClass = MessageAttachment::class.java
        val filenameField = msgClass.getDeclaredField("filename").apply { setAccessible(true) }
        val idField = msgClass.getDeclaredField("id").apply { setAccessible(true) }
        val urlField = msgClass.getDeclaredField("url").apply { setAccessible(true) }
        val proxyUrlField = msgClass.getDeclaredField("proxyUrl").apply { setAccessible(true) }

        val attachment = ReflectUtils.allocateInstance(MessageAttachment::class.java)
        try {
            filenameField.set(attachment, "%s.webp".format(webhook.name))
            idField.set(attachment, SnowflakeUtils.fromTimestamp(System.currentTimeMillis()))
            urlField.set(attachment, webhook.avatarUrl+"?size=2048")
            proxyUrlField.set(attachment, webhook.avatarUrl+"?size=2048")
        } catch (err: Throwable) {
            return
        }

        WidgetChatListAdapterItemAttachment.Companion.`access$navigateToAttachment`(WidgetChatListAdapterItemAttachment.Companion, view?.context, attachment)
    }

    private fun rename(newName: String) {
        Http.Request("https://discord.com/api/v9/webhooks/%s".format(webhook.id), "PATCH")
                .setHeader("Authorization", ReflectUtils.getField(StoreStream.getAuthentication(), "authToken") as String?)
                .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
                .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
                .setHeader("Accept", "*/*")
                .executeWithJson(WebhookRequest(newName))
        Utils.showToast("Webhook renamed")
        parent.fetchList()
        dismiss()
    }
}