package com.aliucord.plugins

import android.content.Context
import com.discord.api.commands.ApplicationCommandType
import com.discord.models.commands.ApplicationCommandOption
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

import com.aliucord.Utils
import com.aliucord.utils.ReflectUtils
import com.aliucord.plugins.ui.EmbedModal

@AliucordPlugin
class SendEmbeds : Plugin() {
    
    override fun start(context: Context) {

        commands.registerCommand(
            "embed",
            "Send Embeds",
            emptyList()
        ) { ctx -> 
            EmbedModal(ctx.getChannelId()).show(Utils.appActivity.supportFragmentManager, "Send Embed")
            return@registerCommand null
        }
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
    }
}
