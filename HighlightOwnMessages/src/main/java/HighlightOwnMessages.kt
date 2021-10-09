package com.aliucord.plugins

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PinePatchFn
import com.aliucord.patcher.Patcher
import com.aliucord.Utils
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.stores.StoreUser
import com.discord.stores.StoreStream
import android.content.Context
import android.view.View
import android.view.Gravity
import android.widget.TextView
import androidx.core.view.GravityCompat

@AliucordPlugin
class HighlightOwnMessages : Plugin() {
    override fun start(context: Context) {
        val userId = StoreStream.getUsers().me.id
        val textViewId = Utils.getResId("chat_list_adapter_item_text", "id")
        with(WidgetChatListAdapterItemMessage::class.java) {
            patcher.patch(getDeclaredMethod("onConfigure", Int::class.java, ChatListEntry::class.java), PinePatchFn { callFrame -> try {
                val message = callFrame.args[1] as MessageEntry

                val view = callFrame.thisObject as WidgetChatListAdapterItemMessage
                val textView = (view.itemView.findViewById(textViewId) as TextView)
                //val replyBar = (view.itemView.findViewById(Utils.getResId("chat_list_adapter_item_text_decorator_reply_link_icon", "id")) as View?)

                val shouldFlip = message.author?.userId == userId
                if (shouldFlip) {
                    view.itemView.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    textView.gravity = GravityCompat.END
                } else {
                    view.itemView.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    textView.gravity = GravityCompat.START
                }

            } catch (ignored: Throwable) {
                Patcher.logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}