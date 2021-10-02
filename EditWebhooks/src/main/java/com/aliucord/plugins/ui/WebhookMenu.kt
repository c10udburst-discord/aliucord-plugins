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

import com.aliucord.Http
import com.google.gson.JsonObject
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.analytics.AnalyticSuperProperties
import com.aliucord.utils.ReflectUtils
import com.discord.stores.StoreStream
import androidx.fragment.app.FragmentManager

class WebhookMenu(private val webhook: Webhook) : AppBottomSheet() {
    

    override fun getContentViewResId() = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View {
        val context = inflater.context
        val layout = LinearLayout(context)
        layout.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))

        val deleteIcon = ContextCompat.getDrawable(context, R.d.ic_delete_24dp)
        val copyIcon = ContextCompat.getDrawable(context, R.d.ic_copy_24dp)

        val title = TextView(context, null, 0, R.h.UiKit_Settings_Item_Header).apply {
            text = webhook.name
        }
        
        val deleteWebhook = TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
            text = "Delete webhook"
            setCompoundDrawablesRelativeWithIntrinsicBounds(deleteIcon, null, null, null)
            setOnClickListener {
                val coDialog = ConfirmDialog()
                                .setTitle("Delete webhook")
                                .setDescription("Do you want to delete \"${webhook.name}\" webhook?")
                coDialog.setOnOkListener {
                    coDialog.dismiss()
                    Utils.threadPool.execute { deleteWebhook(webhook.id) }
            
                }
                coDialog.show(parentFragmentManager, "aaaaaa")
            }
            setClickable(true)
        }

        val copyWebhook = TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
            text = "Copy webhook url"
            setCompoundDrawablesRelativeWithIntrinsicBounds(copyIcon, null, null, null)
            setOnClickListener {
                Utils.setClipboard("Webhook Url", webhook.url)
                Utils.showToast(context, "Webhook url copied to clipboard")
                dismiss()
            }
            setClickable(true)
        }

        layout.addView(title)
        layout.addView(deleteWebhook)
        layout.addView(copyWebhook)

        return layout
    }

    private fun deleteWebhook(id: String?) {
        Http.Request("https://discord.com/api/v9/webhooks/%s".format(id), "DELETE")
                .setHeader("Authorization", ReflectUtils.getField(StoreStream.getAuthentication(), "authToken") as String?)
                .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
                .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
                .setHeader("Accept", "*/*")
                .execute()
        Utils.showToast(context, "Webhook deleted")
        dismiss()
    }
}