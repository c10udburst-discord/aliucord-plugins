package com.aliucord.plugins.ui

import android.annotation.SuppressLint
import android.content.Context
import android.widget.GridLayout
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.plugins.utils.Webhook
import com.aliucord.utils.DimenUtils
import com.aliucord.views.*
import com.aliucord.widgets.LinearLayout
import com.discord.utilities.color.ColorCompat
import com.google.android.material.card.MaterialCardView
import com.aliucord.fragments.ConfirmDialog
import com.lytefast.flexinput.R

import com.aliucord.Http
import com.google.gson.JsonObject
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.analytics.AnalyticSuperProperties
import com.aliucord.utils.ReflectUtils
import com.discord.stores.StoreStream
import androidx.fragment.app.FragmentManager

@SuppressLint("ViewConstructor")
class WebhookCard(ctx: Context, webhook: Webhook, fragmentManager: FragmentManager) : MaterialCardView(ctx) {

    init {
        radius = DimenUtils.getDefaultCardRadius().toFloat()
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondary))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        val root = LinearLayout(ctx)
        root.layoutParams = layoutParams

        val p = DimenUtils.getDefaultPadding()
        val p2 = p / 2

        val title =
                TextView(ctx, null, 0, R.h.UiKit_Settings_Item_Header).apply {
                    text = webhook.name
                    setPadding(p, p, p, p2)
                }
        root.addView(title)

        val btnGrid =
                GridLayout(ctx).apply {
                    rowCount = 1
                    columnCount = 2
                    useDefaultMargins = true
                    setPadding(p2, 0, p2, 0)
                }

        val copyBtn =
                Button(ctx).apply {
                    text = "Copy Url"
                    setOnClickListener {
                        Utils.setClipboard("Webhook Url", webhook.url)
                        Utils.showToast(ctx, "Webhook url copied to clipboard")
                    }
                }
        btnGrid.addView(copyBtn)

        val deleteBtn =
                DangerButton(ctx).apply {
                    text = "Delete Webhook"
                    setOnClickListener {
                        val coDialog = ConfirmDialog()
                                        .setTitle("Delete webhook")
                                        .setDescription("Do you want to delete this webhook?")
                        coDialog.setOnOkListener {
                            coDialog.dismiss()
                            Utils.threadPool.execute { deleteWebhook(webhook.id) }
                    
                        }
                        coDialog.show(fragmentManager, "aaaaaa")
                    }
                }
        btnGrid.addView(deleteBtn)

        root.addView(btnGrid)
        addView(root)
    }

    private fun deleteWebhook(id: String?) {
        Http.Request("https://discord.com/api/v9/webhooks/%s".format(id), "DELETE")
                .setHeader("Authorization", ReflectUtils.getField(StoreStream.getAuthentication(), "authToken") as String?)
                .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
                .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
                .setHeader("Accept", "*/*")
                .execute()
    }
}
