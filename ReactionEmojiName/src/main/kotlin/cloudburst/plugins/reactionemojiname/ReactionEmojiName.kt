package cloudburst.plugins.reactionemojiname

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher

import android.content.Context
import android.os.Build

import com.discord.widgets.chat.managereactions.ManageReactionsEmojisAdapter
import com.discord.databinding.WidgetManageReactionsEmojiBinding

@AliucordPlugin
class MoreTags : Plugin() {

    override fun start(context: Context) {
        with(ManageReactionsEmojisAdapter.ReactionEmojiViewHolder::class.java) {
            val field = getDeclaredField("binding")
            field.isAccessible = true

            patcher.patch(getDeclaredMethod("onConfigure", Int::class.java, ManageReactionsEmojisAdapter.ReactionEmojiItem::class.java), Hook { callFrame -> try {
                val binding = field.get(callFrame.thisObject) as WidgetManageReactionsEmojiBinding
                val emojiItem = callFrame.args[1] as ManageReactionsEmojisAdapter.ReactionEmojiItem
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.a.setTooltipText(emojiItem.reaction.b().d())
                }
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
