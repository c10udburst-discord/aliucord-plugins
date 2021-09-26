package com.aliucord.plugins

import android.content.Context
import com.discord.api.commands.ApplicationCommandType
import com.discord.models.commands.ApplicationCommandOption
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.stores.StoreAuthentication
import com.discord.models.domain.NonceGenerator
import com.discord.utilities.time.ClockFactory
import com.aliucord.plugins.utils.Message
import com.discord.utilities.rest.RestAPI
import com.aliucord.utils.GsonUtils
import com.aliucord.Http
import com.google.gson.JsonObject
import com.discord.stores.StoreStream
import com.aliucord.api.CommandsAPI.CommandResult

val StoreAuthentication.authToken: String
    get() = this.`authToken$app_productionBetaRelease`

// TODO: maybe dont hardcode this lol
const val X_SUPER_PROPERTIES: String = "eyJvcyI6ImFuZHJvaWQiLCJicm93c2VyIjoiZGlzY29yZCBjbGllbnQiLCJyZWxlYXNlX2NoYW5uZWwiOiJiZXRhIiwiY2xpZW50X3ZlcnNpb24iOiI4OS44Iiwib3NfdmVyc2lvbiI6IjExIiwib3NfYXJjaCI6ImFybTY0LXY4YSIsInN5c3RlbV9sb2NhbGUiOiJlbiIsImNsaWVudF9idWlsZF9udW1iZXIiOjg5MTA4LCJjbGllbnRfZXZlbnRfc291cmNlIjpudWxsfQ=="

@AliucordPlugin
class SendEmbeds : Plugin() {
    
    override fun start(context: Context) {

        val argument = ApplicationCommandOption(
            ApplicationCommandType.STRING,
            "json",
            "JSON encoded embed",
            null,
            true,
            true,
            null,
            null
        )
        commands.registerCommand(
            "embed",
            "Send Embeds",
            listOf(argument)
        ) { ctx -> 
            val json = ctx.getRequiredString("json")

            sendJson(json, ctx.getChannelId())

            return@registerCommand CommandResult(null, null, false)
        }
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
    }

    fun sendJson(json: String, channel: Long) {
        val msg: Message = Message(".",
            false,
            NonceGenerator.computeNonce(ClockFactory.get()).toString(), 
            GsonUtils.fromJson(json, JsonObject::class.java)
        )

        Http.Request("https://discord.com/api/v9/channels/%d/messages".format(channel), "POST")
            .setHeader("Authorization", StoreStream.getAuthentication().authToken)
            .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
            .setHeader("X-Super-Properties", X_SUPER_PROPERTIES)
            .setHeader("Accept", "*/*")
            .executeWithJson(msg)
    }
}