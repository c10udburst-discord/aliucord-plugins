package cloudburst.plugins.highlightownmessages

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
import com.lytefast.flexinput.R
import com.discord.utilities.color.ColorCompat

@AliucordPlugin
class HighlightOwnMessages : Plugin() {
    init {
        settingsTab = SettingsTab(Settings::class.java, SettingsTab.Type.PAGE).withArgs(settings)
    }

    override fun start(context: Context) {
        val userId = StoreStream.getUsers().me.id

        val textViewId = Utils.getResId("chat_list_adapter_item_text", "id")
        
        with(WidgetChatListAdapterItemMessage::class.java) {
            patcher.patch(getDeclaredMethod("onConfigure", Int::class.java, ChatListEntry::class.java), Hook { callFrame -> try {
                val message = callFrame.args[1] as MessageEntry

                val view = callFrame.thisObject as WidgetChatListAdapterItemMessage
                val textView = (view.itemView.findViewById(textViewId) as TextView)

                val padding = settings.getInt("Padding", 256)

                if (settings.getBool("RightLeft", true)) {
                    if (message.author?.userId == userId) {
                        view.itemView.layoutDirection = View.LAYOUT_DIRECTION_RTL
                        if (textView.lineCount > 1 && settings.getBool("Multiline", false)) {
                            textView.setPadding(padding, textView.paddingTop, 0, textView.paddingBottom)
                            textView.gravity = Gravity.START
                        } else {
                            textView.setPadding(padding, textView.paddingTop, 0, textView.paddingBottom)
                            textView.gravity = Gravity.END
                        }
                    } else {
                        view.itemView.layoutDirection = View.LAYOUT_DIRECTION_LTR
                        textView.setPadding(0, textView.paddingTop, 0, textView.paddingBottom)
                        textView.gravity = Gravity.START
                    }
                } else {
                    if (message.author?.userId == userId) { textView.setPadding(padding, textView.paddingTop, 0, textView.paddingBottom) }
                    else { textView.setPadding(0, textView.paddingTop, 0, textView.paddingBottom) }
                }

                val selfFg = settings.getInt("SelfFg", 0)
                if (selfFg != 0) {
                    textView.setTextColor(if (message.author?.userId == userId) selfFg else ColorCompat.getColor(context, R.c.primary_dark_200))
                }

                val selfBg = settings.getInt("SelfBg", 0)
                if (selfBg != 0) {
                    textView.setBackgroundColor(if (message.author?.userId == userId) selfBg else 0)
                }

            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}