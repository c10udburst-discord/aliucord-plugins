package com.aliucord.plugins

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
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
import android.widget.FrameLayout

@AliucordPlugin
class HighlightOwnMessages : Plugin() {
    override fun start(context: Context) {
        val userId = StoreStream.getUsers().me.id

        val textViewId = Utils.getResId("chat_list_adapter_item_text", "id")
        
        with(WidgetChatListAdapterItemMessage::class.java) {
            patcher.patch(getDeclaredMethod("onConfigure", Int::class.java, ChatListEntry::class.java), Hook { callFrame -> try {
                val message = callFrame.args[1] as MessageEntry

                val view = callFrame.thisObject as WidgetChatListAdapterItemMessage
                val textView = (view.itemView.findViewById(textViewId) as TextView)
                
                if (message.author?.userId == userId) {
                    view.itemView.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    if (textView.lineCount > 1) {
                        textView.setPadding(256, textView.paddingTop, 0, textView.paddingBottom)
                        textView.gravity = Gravity.START
                    } else {
                        textView.setPadding(256, textView.paddingTop, 0, textView.paddingBottom)
                        textView.gravity = Gravity.END
                    }
                } else {
                    view.itemView.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    textView.setPadding(0, textView.paddingTop, 0, textView.paddingBottom)
                    textView.gravity = Gravity.START
                }

            } catch (ignored: Throwable) {
                Patcher.logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}