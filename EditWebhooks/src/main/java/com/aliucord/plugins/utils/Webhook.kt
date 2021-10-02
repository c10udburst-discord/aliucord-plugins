package com.aliucord.plugins.utils

data class Webhook(
    val avatar: String?,
    val token: String?,
    val name: String?,
    val id: String?
) {
    val url: String
        get() {
            return "https://discord.com/api/webhooks/%s/%s".format(id, token)
        }
    val avatarUrl: String
        get() {
            return "https://cdn.discordapp.com/avatars/%s/%s.webp?size=128".format(id, avatar)
        }
}