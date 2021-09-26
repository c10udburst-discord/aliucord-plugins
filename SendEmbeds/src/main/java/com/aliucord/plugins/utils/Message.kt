package com.aliucord.plugins.utils

import com.google.gson.JsonObject

data class Message(var content: String, var tts: Boolean, var nonce: String, var embed: JsonObject) {}
