package com.aliucord.plugins.embedmodal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.EditText
import com.aliucord.widgets.BottomSheet
import com.aliucord.views.TextInput
import androidx.appcompat.widget.Toolbar
import android.view.inputmethod.EditorInfo
import com.aliucord.views.Button
import com.aliucord.utils.DimenUtils
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

fun View.setMarginEnd(
    value: Int
) {
    val params = Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT)
    params.gravity = Gravity.END
    params.bottomMargin = value
    this.layoutParams = params
}

class EmbedModal(val channelId: Long) : BottomSheet() {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)
        val context = requireContext()
        val padding = DimenUtils.getDefaultPadding()
        val p = padding / 2;
        this.setPadding(padding)

        val authorInput = TextInput(context).apply { 
            hint = "Author"
            editText?.apply { 
                inputType = (EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE or EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            setMarginEnd(p)
        }
        
        val titleInput = TextInput(context).apply { 
            hint = "Title"
            editText?.apply { 
                inputType = (EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE or EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            setMarginEnd(p)
        }

        val contentInput = TextInput(context).apply { 
            hint = "Content"
            editText?.apply { 
                maxLines = Int.MAX_VALUE
                inputType = (EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE or 
                    EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE or 
                    EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_DONE
                setHorizontallyScrolling(false)
            }
            setMarginEnd(p)
        }
        
        val urlInput = TextInput(context).apply { 
            hint = "Url"
            editText?.apply {
                inputType = (EditorInfo.TYPE_TEXT_VARIATION_URI or EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            setMarginEnd(p)
        }
        
        val colorInput = TextInput(context).apply { 
            editText?.setText("#738ADB")
            hint = "Color"
            editText?.apply { 
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            setMarginEnd(p)
        }

        val sendBtn = Button(context).apply { 
            text = "Send"
            setOnClickListener {
                try {
                    Utils.threadPool.execute(object : Runnable {
                        override fun run() {
                            sendNonBotEmbed(
                                authorInput.editText?.text.toString(), 
                                titleInput.editText?.text.toString(), 
                                contentInput.editText?.text.toString(), 
                                urlInput.editText?.text.toString(), 
                                toColorInt(colorInput.editText?.text.toString())
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

        addView(authorInput)
        addView(titleInput)
        addView(contentInput)
        addView(urlInput)
        addView(colorInput)
        addView(sendBtn)
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
