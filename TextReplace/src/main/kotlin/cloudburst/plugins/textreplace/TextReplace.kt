package cloudburst.plugins.textreplace

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.aliucord.Utils
import android.content.Context
import android.widget.FrameLayout
import com.discord.models.message.Message
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

        with(Message::class.java) {
            patcher.patch(getDeclaredMethod("getContent"), Hook { callFrame -> try {
                var _this = callFrame.thisObject as Message?
                val isSent = _this?.hit ?: false
                var content = ReflectUtils.getField(_this!!, "content") as String
                for (rule in TextReplace.replacementRules) {
                    if (isSent and !rule.matchSent) continue
                    if (!isSent and !rule.matchUnsent) continue
                    if (rule.matches(content)) {
                        content = rule.replace(content)
                    }
                }
                callFrame.result = content
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