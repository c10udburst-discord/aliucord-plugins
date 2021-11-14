package cloudburst.plugins.moretags

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.PreHook
import com.aliucord.patcher.Patcher
import com.aliucord.Utils
import android.content.Context
import android.view.View

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.models.message.Message
import com.aliucord.wrappers.ChannelWrapper
import com.discord.api.user.User
import com.discord.stores.StoreStream
import android.widget.TextView

@AliucordPlugin
class MoreTags : Plugin() {

    //val cache: HashMap<Pair<Long, Long>, String?> = hashMapOf();

    override fun start(context: Context) {
        with(WidgetChatListAdapterItemMessage::class.java) { 
            patcher.patch(getDeclaredMethod("configureItemTag", Message::class.java), Hook { callFrame -> try {
                val tag = getDeclaredField("itemTag").let {
                    it.isAccessible = true
                    it.get(callFrame.thisObject) as TextView?
                }
                if (tag == null) return@Hook

                val msg = callFrame.args[0] as Message

                if (msg.webhookId != null && msg.author.f() == "0000") {
                    tag.apply {
                        text = "WEBHOOK";
                        visibility = View.VISIBLE
                    }
                    return@Hook
                }

                val channel = ChannelWrapper(StoreStream.getChannels().getChannel(msg.channelId))
                if (!channel.isGuild()) return@Hook

                val tagStr = getTag(channel.guildId, msg.author)
                if (tagStr != null) {
                    tag.apply {
                        text = tagStr;
                        visibility = View.VISIBLE
                    }
                }

            } catch (ignored: Throwable) {
                Patcher.logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun getTag(guildId: Long, user: User?): String? {
        if (user == null) return null
        // val cacheEntry = Pair(guildId, user.i())
        // if (cache.containsKey(cacheEntry)) return cache.get(cacheEntry)

        val guild = StoreStream.getGuilds().getGuild(guildId)

        if (guild == null) return null;

        if (guild.isOwner(user.i())) {
            //cache.put(cacheEntry, "OWNER")
            return "OWNER";
        }

        return null
    }
}