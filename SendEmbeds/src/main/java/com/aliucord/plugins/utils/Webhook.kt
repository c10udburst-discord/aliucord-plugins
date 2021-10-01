package com.aliucord.plugins.utils

data class Webhook(
    val token: String?,
    val name: String?,
    val id: String?
) {
    val url: String
        get() {
            return "https://discord.com/api/webhooks/%s/%s".format(id, token)
        }
}