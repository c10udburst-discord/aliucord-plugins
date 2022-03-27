package cloudburst.plugins.deleteembeds

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
import com.aliucord.patcher.Hook
import com.discord.databinding.WidgetChatListActionsBinding
import com.discord.utilities.color.ColorCompat
import com.discord.stores.StoreStream
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.lytefast.flexinput.R
import java.lang.reflect.InvocationTargetException
import com.aliucord.Utils
import com.aliucord.utils.GsonUtils
import com.aliucord.Http
import com.google.gson.JsonObject

@AliucordPlugin
class DeleteEmbeds : Plugin() {
    @SuppressLint("SetTextI18n")
    override fun start(context: Context) {
        val icon = ContextCompat.getDrawable(context, R.e.ic_clear_24dp)
        val deleteEmbedId = View.generateViewId()

        with(WidgetChatListActions::class.java, {
            val getBinding = getDeclaredMethod("getBinding").apply { isAccessible = true }

            patcher.patch(getDeclaredMethod("configureUI", WidgetChatListActions.Model::class.java), Hook { callFrame ->
                try {
                    val message = (callFrame.args[0] as WidgetChatListActions.Model).message

                    val binding = getBinding.invoke(callFrame.thisObject) as WidgetChatListActionsBinding
                    val deleteEmbed = binding.a.findViewById<TextView>(deleteEmbedId).apply {
                        if (message.hasEmbeds()) {
                            visibility = if ((callFrame.args[0] as WidgetChatListActions.Model).manageMessageContext.canDelete) 
                                View.VISIBLE else View.GONE
                        } else if (message.hasAttachments() && !message.content.isEmpty()) {
                            visibility = if (message.author.id == StoreStream.getUsers().me.id) 
                                View.VISIBLE else View.GONE
                        } else {
                            visibility = View.GONE
                        }
                    }

                    if (!deleteEmbed.hasOnClickListeners()) deleteEmbed.setOnClickListener {
                        try {
                             Utils.threadPool.execute {
                                if (message.hasEmbeds()) {
                                    deleteEmbed(message.channelId, message.id)
                                }
                                if (message.hasAttachments() && message.author.id == StoreStream.getUsers().me.id) {
                                    deleteAttachements(message.channelId, message.id)
                                }
                             }
                             (callFrame.thisObject as WidgetChatListActions).dismiss()
                        } catch (e: IllegalAccessException) {
                            Utils.showToast("Internal error occured.")
                            e.printStackTrace()
                        } catch (e: InvocationTargetException) {
                            Utils.showToast("Internal error occured.")
                            e.printStackTrace()
                        }
                    }
                } catch (ignored: Throwable) {
                }
            })

            patcher.patch(getDeclaredMethod("onViewCreated", View::class.java, Bundle::class.java), Hook { callFrame ->
                val linearLayout = (callFrame.args[0] as NestedScrollView).getChildAt(0) as LinearLayout
                val ctx = linearLayout.context
                val idx = linearLayout.indexOfChild(linearLayout.findViewById(Utils.getResId("dialog_chat_actions_delete", "id"))) + 1

                icon?.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))

                val deleteEmbed = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                    text = "Delete Embed"
                    id = deleteEmbedId
                    setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
                    typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
                }

                linearLayout.addView(deleteEmbed, idx)
            })
        })
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    fun deleteEmbed(channelId: Long, msgId: Long) {
        Http.Request.newDiscordRequest("/channels/%d/messages/%d".format(channelId, msgId), "PATCH")
            .setHeader("Referer", "https://discord.com/channels/%d/%d".format(channelId, msgId))
            .executeWithJson(GsonUtils.fromJson("{\"flags\":4}", JsonObject::class.java))
    }

    fun deleteAttachements(channelId: Long, msgId: Long) {
        Http.Request.newDiscordRequest("/channels/%d/messages/%d".format(channelId, msgId), "PATCH")
            .setHeader("Referer", "https://discord.com/channels/%d/%d".format(channelId, msgId))
            .executeWithJson(GsonUtils.fromJson("{\"attachments\":[]}", JsonObject::class.java))
    }
}
