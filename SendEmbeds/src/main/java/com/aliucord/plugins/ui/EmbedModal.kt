package com.aliucord.plugins.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.EditText
import com.aliucord.widgets.BottomSheet
import com.aliucord.widgets.LinearLayout
import com.aliucord.views.TextInput
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import android.view.inputmethod.EditorInfo
import com.aliucord.views.Button
import com.aliucord.utils.DimenUtils
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R
import com.discord.utilities.colors.ColorPickerUtils
import com.aliucord.plugins.ui.ModeSelector

import com.aliucord.plugins.utils.Author
import com.aliucord.plugins.utils.Embed
import com.aliucord.plugins.utils.Message
import com.aliucord.Http

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
import com.jaredrummler.android.colorpicker.ColorPickerDialog
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
        val context = view.context
        val padding = DimenUtils.getDefaultPadding()
        val p = padding / 2;
        this.setPadding(padding)

        val authorInput = TextInput(context).apply { 
            hint = "Author"
            editText?.apply {
                setText(StoreStream.getUsers().me.username)
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
                inputType = EditorInfo.TYPE_NULL
                setOnClickListener {
                    val builder = ColorPickerUtils.INSTANCE.buildColorPickerDialog(
                        context, 
                        Utils.getResId("color_picker_title", "string"), 
                        ColorCompat.getThemedColor(context, R.b.colorAccent)
                    )
                    builder.j = object: c.k.a.a.f { // color picker listener i guess
                        override fun onColorReset(i: Int) { }

                        override fun onColorSelected(i: Int, i2: Int) {
                            editText?.setText("#%06X".format(i2 and 0x00FFFFFF)) // remove alpha component
                        }

                        override fun onDialogDismissed(i: Int) { }
                    }
                    builder.show(parentFragmentManager, "COLOR_PICKER")
                }
                setClickable(true)
            }
            setMarginEnd(p)
        }

        val modeInput = TextInput(context).apply { 
            editText?.setText("embed.rauf.workers.dev")
            hint = "Mode"
            editText?.apply { 
                inputType = EditorInfo.TYPE_NULL
                setOnClickListener {
                    val modeSelector = ModeSelector(listOf(
                        "embed.rauf.workers.dev",
                        "embed.rauf.wtf"
                        "selfbot"
                    ), {mode -> 
                        this.setText(mode)
                    })
                    modeSelector.show(parentFragmentManager, "Embed Mode")
                }
            }
            setMarginEnd(p)
        }

        val sendBtn = Button(context).apply { 
            text = "Send"
            setOnClickListener {
                try {
                    Utils.threadPool.execute(object : Runnable {
                        override fun run() {
                            val mode = modeInput.editText?.text.toString()
                            if (mode == "selfbot") {
                                sendSelfBotEmbed(
                                    authorInput.editText?.text.toString(), 
                                    titleInput.editText?.text.toString(), 
                                    contentInput.editText?.text.toString(), 
                                    urlInput.editText?.text.toString(), 
                                    toColorInt(colorInput.editText?.text.toString())
                                )
                            } else {
                                sendNonBotEmbed(
                                    "https://"+mode+"/",
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
        addView(modeInput)
        addView(sendBtn)
    }

    private fun sendSelfBotEmbed(author: String, title: String, content: String, url: String, color: Int) {
        try {
            val msg = Message(
                null,
                false,
                NonceGenerator.computeNonce(ClockFactory.get()).toString(),
                Embed(
                    Author(author),
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

    private fun sendNonBotEmbed(site: String, author: String, title: String, content: String, url: String, color: Int) {
        val msg = "[](%s?author=%s&title=%s&description=%s&color=%06x&redirect=%s)".format(site, URLEncoder.encode(author, "utf-8"), URLEncoder.encode(title, "utf-8"), URLEncoder.encode(content, "utf-8"), color, URLEncoder.encode(url, "utf-8"))
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
            return a
                .replace("#", "")
                .toInt(16)
        } catch(e:Throwable) {
            Utils.showToast(context, "Color parser error: %s".format(e.message))
            e.printStackTrace()
        }
        return 0
    }

}
