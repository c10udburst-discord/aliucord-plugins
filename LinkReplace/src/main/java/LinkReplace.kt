package com.aliucord.plugins

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.aliucord.Utils
import android.content.Context
import android.widget.FrameLayout
import com.discord.models.message.Message
import com.aliucord.utils.ReflectUtils
import com.aliucord.plugins.utils.LinkReplacement
import com.aliucord.api.SettingsAPI
import com.aliucord.plugins.ui.ReplacerSettings

@AliucordPlugin
class LinkReplace : Plugin() {

    init {
        settingsTab = SettingsTab(ReplacerSettings::class.java)
    }

    override fun start(context: Context) {
        mSettings = settings
        val replacementRules = settings.getObject("LinkReplace_Rules", LinkReplacement.DEFAULT_LIST, Array<LinkReplacement>::class.java)

        with(Message::class.java) {
            patcher.patch(getDeclaredMethod("getContent"), Hook { callFrame -> try {
                var _this = callFrame.thisObject as Message
                var content = ReflectUtils.getField(_this, "content") as String
                for (rule in replacementRules) {
                    if (rule.fromRegex.containsMatchIn(content)) {
                        content = rule.fromRegex.replace(content, rule.toDomain)
                        Patcher.logger.info(content)
                        callFrame.result = content
                        break
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
    }
}