package com.aliucord.plugins.embedmodal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.AutoCompleteTextView
import android.widget.MultiAutoCompleteTextView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Button
import com.aliucord.widgets.BottomSheet
import com.aliucord.Utils
import com.aliucord.utils.GsonUtils

import com.aliucord.utils.ReflectUtils
import com.discord.stores.StoreStream
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.analytics.AnalyticSuperProperties

import com.aliucord.Http
import com.aliucord.plugins.utils.Embed
import com.aliucord.plugins.utils.Message
import com.discord.models.domain.NonceGenerator
import com.discord.utilities.time.ClockFactory

import com.discord.restapi.RestAPIParams
import com.aliucord.utils.RxUtils.createActionSubscriber
import com.aliucord.utils.RxUtils.subscribe
import java.net.URLEncoder


class EmbedModal(val channelId: Long) : BottomSheet() {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        val context = view.context
        // val selfbotCheckbox = CheckBox(context).apply { text = "Self-bot mode" }
        // linearLayout.addView(selfbotCheckbox)

        val titleInput = AutoCompleteTextView(context).apply { 
            hint = "Title"
        }
        linearLayout.addView(titleInput)

        val contentInput = MultiAutoCompleteTextView(context).apply { 
            hint = "Content"
        }
        linearLayout.addView(contentInput)

        val urlInput = AutoCompleteTextView(context).apply { 
            hint = "Url"
        }
        linearLayout.addView(urlInput)

        val colorInput = EditText(context).apply { 
            setText("#738ADB", TextView.BufferType.EDITABLE)
            hint = "Color"
        }
        linearLayout.addView(colorInput)

        val sendBtn = Button(context).apply { 
            text = "Send"
            setOnClickListener {
                try {
                    // if (selfbotCheckbox.isChecked) {
                    //     Utils.threadPool.execute(object : Runnable {
                    //         override fun run() {
                    //             sendSelfbotEmbed(titleInput.text.toString(), contentInput.text.toString(), urlInput.text.toString(), toColorInt(colorInput.text.toString()))
                    //         }
                    //     })
                    // } else {
                        Utils.threadPool.execute(object : Runnable {
                            override fun run() {
                                sendNonBotEmbed(titleInput.text.toString(), contentInput.text.toString(), urlInput.text.toString(), toColorInt(colorInput.text.toString()))
                            }
                        })
                    // }
                    dismiss()
                } catch (e: Throwable) {
                    Utils.showToast(context, "An error occured")
                    e.printStackTrace()
                }
                
            }
        }
        linearLayout.addView(sendBtn)
    }

    // private fun sendSelfbotEmbed(title: String, content: String, url: String, color: Int) {
    //     try {
    //         val msg = Message(
    //             null,
    //             false,
    //             NonceGenerator.computeNonce(ClockFactory.get()).toString(),
    //             Embed(
    //                 title, 
    //                 content,
    //                 url,
    //                 color
    //             )
    //         )
    //         Http.Request("https://discord.com/api/v9/channels/%d/messages".format(channelId), "POST")
    //             .setHeader("Authorization", ReflectUtils.getField(StoreStream.getAuthentication(), "authToken") as String?)
    //             .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
    //             .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
    //             .setHeader("Accept", "*/*")
    //             .executeWithJson(msg)
    //         .text()
    //     } catch (e: Throwable) {
    //         e.printStackTrace()
    //     }
    // }

    private fun sendNonBotEmbed(title: String, content: String, url: String, color: Int) {
        val msg = "https://embed.rauf.workers.dev/?title=%s&description=%s&color=%h&redirect=%s".format(URLEncoder.encode(title, "utf-8"), URLEncoder.encode(content, "utf-8"), color, URLEncoder.encode(url, "utf-8"))
        val message = RestAPIParams.Message(
            msg,
            NonceGenerator.computeNonce(ClockFactory.get()).toString(),
            null,
            null,
            emptyList(),
            null,
            RestAPIParams.Message.AllowedMentions(
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    false
            )
        )
        RestAPI.api.sendMessage(channelId, message).subscribe(createActionSubscriber({ }))
    }

    private fun toColorInt(a: String): Int {
        try {
            if (a.startsWith("#")) return a
                .replace("#", "")
                .toInt(16)
        } catch(e:Throwable) {
            Utils.showToast(context, "Color parser error: %s".format(e.message))
            e.printStackTrace()
        }
        return 0
    }
}
