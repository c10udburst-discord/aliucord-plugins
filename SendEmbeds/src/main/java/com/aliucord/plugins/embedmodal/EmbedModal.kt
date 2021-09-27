package com.aliucord.plugins.embedmodal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.AutoCompleteTextView
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import android.widget.Button
import com.aliucord.widgets.BottomSheet
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

import com.aliucord.Utils
import com.aliucord.utils.GsonUtils

import com.aliucord.utils.ReflectUtils
import com.discord.stores.StoreStream
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.analytics.AnalyticSuperProperties

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
        this.setPadding(24)

        val authorInput = AutoCompleteTextView(context).apply { 
            hint = "Author"
            setTextColor(ColorCompat.getThemedColor(context, R.b.colorTextNormal))
            setHintTextColor(ColorCompat.getThemedColor(context, R.b.colorTextMuted))
        }
        linearLayout.addView(authorInput)

        val titleInput = AutoCompleteTextView(context).apply { 
            hint = "Title"
            setTextColor(ColorCompat.getThemedColor(context, R.b.colorTextNormal))
            setHintTextColor(ColorCompat.getThemedColor(context, R.b.colorTextMuted))
        }
        linearLayout.addView(titleInput)

        val contentInput = MultiAutoCompleteTextView(context).apply { 
            hint = "Content"
            minimumHeight = 120
            setTextColor(ColorCompat.getThemedColor(context, R.b.colorTextNormal))
            setHintTextColor(ColorCompat.getThemedColor(context, R.b.colorTextMuted))
        }
        linearLayout.addView(contentInput)

        val urlInput = AutoCompleteTextView(context).apply { 
            hint = "Url"
            setTextColor(ColorCompat.getThemedColor(context, R.b.colorTextNormal))
            setHintTextColor(ColorCompat.getThemedColor(context, R.b.colorTextMuted))
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
                    Utils.threadPool.execute(object : Runnable {
                        override fun run() {
                            sendNonBotEmbed(
                                authorInput.text.toString(), 
                                titleInput.text.toString(), 
                                contentInput.text.toString(), 
                                urlInput.text.toString(), 
                                toColorInt(colorInput.text.toString())
                            )
                        }
                    })
                    dismiss()
                } catch (e: Throwable) {
                    Utils.showToast(context, "An error occured")
                    e.printStackTrace()
                }
                
            }
        }
        linearLayout.addView(sendBtn)
    }

    private fun sendNonBotEmbed(author: String, title: String, content: String, url: String, color: Int) {
        val msg = "https://embed.rauf.workers.dev/?author=%s&title=%s&description=%s&color=%06x&redirect=%s".format(URLEncoder.encode(author, "utf-8"), URLEncoder.encode(title, "utf-8"), URLEncoder.encode(content, "utf-8"), color, URLEncoder.encode(url, "utf-8"))
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
            if (a.matches(Regex("^#?[0-9A-Fa-f]{6}$"))) return a
                .replace("#", "")
                .toInt(16)
            else if (a.matches(Regex("^[0-9]+$"))) return a.toInt(10)
        } catch(e:Throwable) {
            Utils.showToast(context, "Color parser error: %s".format(e.message))
            e.printStackTrace()
        }
        return 0
    }
}
