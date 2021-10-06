package com.aliucord.plugins

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.Constants
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.fragments.InputDialog
import com.aliucord.patcher.PinePatchFn
import com.discord.databinding.WidgetChatListActionsBinding
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.lytefast.flexinput.R
import top.canyie.pine.Pine.CallFrame
import java.lang.reflect.InvocationTargetException
import com.aliucord.utils.ReflectUtils
import com.discord.stores.StoreStream
import com.aliucord.Utils
import com.aliucord.utils.GsonUtils
import com.aliucord.Http
import com.google.gson.JsonObject
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.analytics.AnalyticSuperProperties

@AliucordPlugin
class DeleteEmbeds : Plugin() {
    @SuppressLint("SetTextI18n")
    override fun start(context: Context) {
        val icon = ContextCompat.getDrawable(context, R.d.ic_clear_24dp)
        val deleteEmbedId = View.generateViewId()

        with(WidgetChatListActions::class.java, {
            val getBinding = getDeclaredMethod("getBinding").apply { isAccessible = true }

            patcher.patch(getDeclaredMethod("configureUI", WidgetChatListActions.Model::class.java), PinePatchFn { callFrame: CallFrame ->
                try {
                    val message = (callFrame.args[0] as WidgetChatListActions.Model).message

                    val binding = getBinding.invoke(callFrame.thisObject) as WidgetChatListActionsBinding
                    val deleteEmbed = binding.a.findViewById<TextView>(deleteEmbedId).apply {
                        if (!message.hasEmbeds()) {
                            visibility = View.GONE
                        } else if (!(callFrame.args[0] as WidgetChatListActions.Model).manageMessageContext.canDelete) {
                            visibility = View.GONE
                        } else {
                            visibility = View.VISIBLE
                        }
                    }

                    if (!deleteEmbed.hasOnClickListeners()) deleteEmbed.setOnClickListener {
                        try {
                             Utils.threadPool.execute {
                                 deleteEmbed(message.channelId, message.id)
                             }
                        } catch (e: IllegalAccessException) {
                            Utils.showToast(context, "Internal error occured.")
                            e.printStackTrace()
                        } catch (e: InvocationTargetException) {
                            Utils.showToast(context, "Internal error occured.")
                            e.printStackTrace()
                        }
                    }
                } catch (ignored: Throwable) {
                }
            })

            patcher.patch(getDeclaredMethod("onViewCreated", View::class.java, Bundle::class.java), PinePatchFn { callFrame: CallFrame ->
                val linearLayout = (callFrame.args[0] as NestedScrollView).getChildAt(0) as LinearLayout
                val ctx = linearLayout.context

                icon?.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))

                val deleteEmbed = TextView(ctx, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
                    text = "Delete Embed"
                    id = deleteEmbedId
                    setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
                    typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
                }

                linearLayout.addView(deleteEmbed, 1)
            })
        })
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    fun deleteEmbed(channelId: Long, msgId: Long) {
        Http.Request("https://discord.com/api/v9/channels/%d/messages/%d".format(channelId, msgId), "PATCH")
            .setHeader("Authorization", ReflectUtils.getField(StoreStream.getAuthentication(), "authToken") as String?)
            .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
            .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
            .setHeader("Referer", "https://discord.com/channels/%d/%d".format(channelId, msgId))
            .setHeader("Accept", "*/*")
            .executeWithJson(GsonUtils.fromJson("{\"flags\":4}", JsonObject::class.java))
    }
}
