package cloudburst.plugins.textreplace

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.aliucord.Utils
import android.content.Context
import android.widget.FrameLayout
import com.aliucord.utils.ReflectUtils
import com.aliucord.api.SettingsAPI

import cloudburst.plugins.textreplace.utils.TextReplacement
import cloudburst.plugins.textreplace.ui.ReplacerSettings

@AliucordPlugin
class TextReplace : Plugin() {
    init {
        settingsTab = SettingsTab(ReplacerSettings::class.java)
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
                var _this = callFrame.thisObject as com.discord.api.message.Message
                var content = ReflectUtils.getField(_this, "content") as String
                for (rule in TextReplace.replacementRules) {
                    if (!rule.matchSent) continue
                    if (rule.matches(content)) {
                        content = rule.replace(content)
                    }
                }
                callFrame.result = content
            } catch (ignored: Throwable) {
                Patcher.logger.error(ignored)
            }})
        }

        with(com.discord.restapi.RestAPIParams.Message::class.java) {
            patcher.patch(getConstructor(), Hook { callFrame -> try {
                var _this = callFrame.thisObject as com.discord.api.message.Message
                var content = ReflectUtils.getField(_this, "content") as String
                for (rule in TextReplace.replacementRules) {
                    if (!rule.matchUnsent) continue
                    if (rule.matches(content)) {
                        content = rule.replace(content)
                    }
                }
            } catch (ignored: Throwable) {
                Patcher.logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    companion object {
        lateinit var mSettings: SettingsAPI
        var replacementRules = TextReplacement.DEFAULT_LIST
    }
}