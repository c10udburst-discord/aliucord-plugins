package cloudburst.plugins.textreplace

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.PreHook
import com.aliucord.patcher.Patcher
import com.aliucord.Utils
import android.content.Context
import android.widget.FrameLayout
import com.aliucord.utils.ReflectUtils
import com.aliucord.api.SettingsAPI
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.chat.MessageManager
import com.discord.stores.StoreStream
import com.discord.api.premium.PremiumTier

import cloudburst.plugins.textreplace.utils.TextReplacement
import cloudburst.plugins.textreplace.ui.ReplacerSettings

@AliucordPlugin
class TextReplace : Plugin() {
    lateinit var pluginIcon: Drawable
    private val textContentField =
    MessageContent::class.java.getDeclaredField("textContent").apply {
        isAccessible = true
    }

    init {
        settingsTab = SettingsTab(ReplacerSettings::class.java)
    }

    override fun load(context: Context) {
        pluginIcon = ContextCompat.getDrawable(context, Utils.getResId("drawable_icon_sync_integration", "drawable"))!!
    }

    override fun start(context: Context) {
        mSettings = settings
        TextReplace.replacementRules = settings.getObject(
            "TextReplace_Rules", 
            TextReplacement.DEFAULT_LIST, 
            Array<TextReplacement>::class.java
        )

        with(com.discord.api.message.Message::class.java) {
            patcher.patch(getDeclaredMethod("i"), Hook { callFrame -> try {
                if (callFrame.result == null) return@Hook
                var content = callFrame.result as String
                if (content == "") return@Hook
                for (rule in TextReplace.replacementRules) {
                    if (!rule.matchSent) continue
                    if (rule.matches(content)) {
                        content = rule.replace(content)
                    }
                }
                callFrame.result = content
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }

        with(com.discord.widgets.chat.input.ChatInputViewModel::class.java) {
            patcher.patch(getDeclaredMethod("sendMessage", Context::class.java, MessageManager::class.java, MessageContent::class.java, List::class.java, Boolean::class.javaPrimitiveType, Function1::class.java), PreHook { callFrame -> try {
                //val isNitro = StoreStream.getUsers().me.premiumTier == PremiumTier.TIER_2
                val messageContent = callFrame.args[2] as MessageContent?
                if (messageContent == null) return@PreHook
                var content = textContentField.get(messageContent) as String? ?:""
                if (content == "") return@PreHook
                for (rule in TextReplace.replacementRules) {
                    if (!rule.matchUnsent) continue
                    if (rule.matches(content)) {
                        content = rule.replace(content)
                    }
                }
                textContentField.set(messageContent, content) //textContentField.set(messageContent, content.take(if (isNitro) 4000 else 2000))
                return@PreHook
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }

        with(com.discord.api.message.embed.MessageEmbed::class.java) {
            patcher.patch(getDeclaredMethod("j"), Hook { callFrame -> try {
                if (callFrame.result == null) return@Hook
                var content = callFrame.result as String
                if (content == "") return@Hook
                for (rule in TextReplace.replacementRules) {
                    if (!rule.matchEmbeds) continue
                    if (rule.matches(content)) {
                        content = rule.replace(content)
                    }
                }
                callFrame.result = content
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})

            patcher.patch(getDeclaredMethod("c"), Hook { callFrame -> try {
                if (callFrame.result == null) return@Hook
                var content = callFrame.result as String
                if (content == "") return@Hook
                for (rule in TextReplace.replacementRules) {
                    if (!rule.matchEmbeds) continue
                    if (rule.matches(content)) {
                        content = rule.replace(content)
                    }
                }
                callFrame.result = content
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    companion object {
        lateinit var mSettings: SettingsAPI
        var replacementRules = TextReplacement.DEFAULT_LIST
    }
}
