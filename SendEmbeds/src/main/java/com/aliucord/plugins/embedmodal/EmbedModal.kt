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


class EmbedModal(val channelId: Long) : BottomSheet() {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        val context = view.context
        val selfbotCheckbox = CheckBox(context).apply { text = "Self-bot mode" }
        linearLayout.addView(selfbotCheckbox)

        val titleInput = AutoCompleteTextView(context).apply { setText("Title", TextView.BufferType.EDITABLE) }
        linearLayout.addView(titleInput)

        val contentInput = MultiAutoCompleteTextView(context).apply { setText("Content", TextView.BufferType.EDITABLE) }
        linearLayout.addView(contentInput)

        val urlInput = AutoCompleteTextView(context).apply { setText("https://example.com", TextView.BufferType.EDITABLE) }
        linearLayout.addView(urlInput)

        val sendBtn = Button(context).apply { 
            text = "Send"
        }

        sendBtn.setOnClickListener {
            try {
                if (selfbotCheckbox.isChecked) {
                    Utils.threadPool.execute(object : Runnable {
                        override fun run() {
                            sendSelfbotEmbed(titleInput.text.toString(), contentInput.text.toString(), 5793266)
                        }
                    })
                } else {
                    Utils.threadPool.execute(object : Runnable {
                        override fun run() {
                            sendNonBotEmbed(titleInput.text.toString(), contentInput.text.toString(), 5793266)
                        }
                    })
                }
                dismiss()
            } catch (e: Throwable) {
                Utils.showToast(context, "An error occured")
                e.printStackTrace()
            }
            
        }
        linearLayout.addView(sendBtn)
    }

    private fun sendSelfbotEmbed(title: String, content: String, url: String, color: Int) {
        try {
            val msg = Message(
                null,
                false,
                NonceGenerator.computeNonce(ClockFactory.get()).toString(),
                Embed(
                    title, 
                    content,
                    url,
                    color
                )
            )
            Http.Request("https://discord.com/api/v9/channels/%d/messages".format(channelId), "POST")
                .setHeader("Authorization", ReflectUtils.getField(StoreStream.getAuthentication(), "authToken") as String?)
                .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
                .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
                .setHeader("Accept", "*/*")
                .executeWithJson(msg)
            .text()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun sendNonBotEmbed(title: String, content: String, url: String, color: Int) {
        val msg = "https://embed.rauf.workers.dev/?title=%s&description=%s&color=%h&redirect=".format(title, content, color, url)
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
}
