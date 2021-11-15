package cloudburst.plugins.sendembeds.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
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
import cloudburst.plugins.sendembeds.ui.ModeSelector

import com.aliucord.Utils
import com.aliucord.utils.GsonUtils

import cloudburst.plugins.sendembeds.utils.*
import cloudburst.plugins.sendembeds.SendEmbeds
import com.aliucord.Http

import com.discord.utilities.permissions.PermissionUtils
import com.discord.api.permission.Permission

import com.aliucord.utils.ReflectUtils
import com.discord.stores.StoreStream
import com.discord.stores.StorePermissions
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.analytics.AnalyticSuperProperties

import com.discord.models.domain.NonceGenerator
import com.discord.utilities.time.ClockFactory

import com.discord.restapi.RestAPIParams
import com.aliucord.utils.RxUtils.createActionSubscriber
import com.aliucord.utils.RxUtils.subscribe
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import java.net.URLEncoder
import java.util.HashMap
import com.aliucord.Logger


fun View.setMarginEnd(
    value: Int
) {
    val params = Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT)
    params.gravity = Gravity.END
    params.bottomMargin = value
    this.layoutParams = params
}

class EmbedModal(val channelId: Long, val plugin: SendEmbeds, private val modeOverride: String?) : BottomSheet() {

    class EmptyTextWatcher(): TextWatcher {
        override public fun afterTextChanged(s: Editable) { }
        override public fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) { }
        override public fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) { }
    }

    private val logger = Logger("SendEmbeds")

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)
        val context = view.context
        val padding = DimenUtils.defaultPadding
        val p = padding / 2;
        this.setPadding(padding)

        val authorInput = TextInput(context, "Author").apply { 
            editText.apply {
                setText(StoreStream.getUsers().me.username)
                inputType = (EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE or EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            setMarginEnd(p)
        }
        
        val titleInput = TextInput(context, "Title").apply { 
            editText.apply { 
                inputType = (EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE or EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            setMarginEnd(p)
        }

        val contentInput = TextInput(context, "Content").apply { 
            editText.apply { 
                maxLines = Int.MAX_VALUE
                inputType = (EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE or 
                    EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE or 
                    EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_DONE
                setHorizontallyScrolling(false)
            }
            setMarginEnd(p)
        }
        
        val urlInput = TextInput(context, "Url").apply { 
            editText.apply {
                inputType = (EditorInfo.TYPE_TEXT_VARIATION_URI or EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            setMarginEnd(p)
        }
        
        val colorInput = TextInput(context, "Color", "#%06X".format(ColorCompat.getThemedColor(context, R.b.colorAccent) and 0x00FFFFFF), EmptyTextWatcher()).apply { 
            editText.apply { 
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
                            editText.setText("#%06X".format(i2 and 0x00FFFFFF)) // remove alpha component
                        }

                        override fun onDialogDismissed(i: Int) { }
                    }
                    builder.show(parentFragmentManager, "COLOR_PICKER")
                }
                setClickable(true)
            }
            setMarginEnd(p)
        }

        val imageInput = TextInput(context, "Image Url").apply { 
            editText.apply {
                inputType = (EditorInfo.TYPE_TEXT_VARIATION_URI or EditorInfo.TYPE_CLASS_TEXT)
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            setMarginEnd(p)
        }

        val modeInput = Button(context).apply { 
            text = "embed.rauf.workers.dev"
            setBackgroundColor(ColorCompat.getThemedColor(view.getContext(), R.b.colorBackgroundTertiary))
            setOnClickListener {
                Utils.threadPool.execute({
                    val webhooks = HashMap<String, Webhook>()
                    val perms = StoreStream.getPermissions()
                    if (PermissionUtils.can(Permission.MANAGE_WEBHOOKS, perms.permissionsByChannel.get(channelId))) {
                        for(hook in getWebhooks()) {
                            if (hook.token == null) continue
                            var name = hook.name
                            if (name == null) {
                                name = hook.token
                            }
                            while (webhooks.containsKey(name)) {
                                name += "."
                            }
                            webhooks.put(name, hook)
                        }
                    }
                    
                    val modes = plugin.modes.toMutableList()

                    webhooks.keys.forEach {
                        modes.add("webhook: %s".format(it))
                    }
                    
                    val modeSelector = ModeSelector(modes, {mode -> 
                        if (mode.startsWith("webhook: ")) {
                            val hook = webhooks.get(mode.drop(9))
                            this.setText("webhooks/%s/%s".format(hook?.id, hook?.token))
                        } else {
                            this.setText(mode)
                        }
                    })
                    modeSelector.show(parentFragmentManager, "Embed Mode")
                    
                })

            }
            setMarginEnd(p)
        }

        val sendBtn = Button(context).apply { 
            text = "Send"
            setOnClickListener {
                try {
                    Utils.threadPool.execute(object : Runnable {
                        override fun run() {
                            onSend(
                                modeOverride ?: modeInput.text.toString(), 
                                authorInput.editText.text.toString(), 
                                titleInput.editText.text.toString(), 
                                contentInput.editText.text.toString(), 
                                urlInput.editText.text.toString(), 
                                imageInput.editText.text.toString(), 
                                colorInput.editText.text.toString()
                            )
                        }
                    })
                } catch (e: Throwable) {
                    Utils.showToast("An error occured")
                    logger.error(e)
                }
                dismiss()
            }
        }

        addView(authorInput)
        addView(titleInput)
        addView(contentInput)
        addView(urlInput)
        addView(imageInput)
        addView(colorInput)
        if (modeOverride == null)
            addView(modeInput)
        addView(sendBtn)
    }

    private fun getWebhooks(): Array<Webhook> {
        try {
            return Http.Request("https://discord.com/api/v9/channels/%d/webhooks".format(channelId), "GET")
                .setHeader("Authorization", ReflectUtils.getField(StoreStream.getAuthentication(), "authToken") as String?)
                .setHeader("User-Agent", RestAPI.AppHeadersProvider.INSTANCE.userAgent)
                .setHeader("X-Super-Properties", AnalyticSuperProperties.INSTANCE.superPropertiesStringBase64)
                .setHeader("Accept", "*/*")
                .execute()
                .json(Array<Webhook>::class.java)
        } catch (e: Throwable) {
            logger.error(e)
        }
        return emptyArray()
    }

    private fun sendWebhookEmbed(webhook: String, author: String, title: String, content: String, url: String, imageUrl: String, color: Int)  {

        Http.Request("https://discord.com/api/%s".format(webhook), "POST")
            .executeWithJson(WebhookMessage(
                null, 
                listOf(
                    Embed(
                        Author(author),
                        title, 
                        content,
                        url,
                        EmbedImage(imageUrl),
                        color
                    )
                )
            ))
    }

    private fun sendNonBotEmbed(site: String, author: String, title: String, content: String, url: String, imageUrl: String, color: Int) {
        val msg = if (plugin.settings.getBool("SendEmbeds_NQNCompatibility", true)) {
            "[](https://%s/?author=%s&title=%s&description=%s&color=%06x&image=%s&redirect=%s)".format(site, URLEncoder.encode(author, "utf-8"), URLEncoder.encode(title, "utf-8"), URLEncoder.encode(content, "utf-8"), color, URLEncoder.encode(imageUrl, "utf-8"), URLEncoder.encode(url, "utf-8"))
        }
        else if (plugin.settings.getBool("SendEmbeds_LinkVisible", true)) {
            """||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||||​||
            https://%s/?author=%s&title=%s&description=%s&color=%06x&image=%s&redirect=%s""".format(site, URLEncoder.encode(author, "utf-8"), URLEncoder.encode(title, "utf-8"), URLEncoder.encode(content, "utf-8"), color, URLEncoder.encode(imageUrl, "utf-8"), URLEncoder.encode(url, "utf-8"))
        }
        else {
            "https://%s/?author=%s&title=%s&description=%s&color=%06x&image=%s&redirect=%s".format(site, URLEncoder.encode(author, "utf-8"), URLEncoder.encode(title, "utf-8"), URLEncoder.encode(content, "utf-8"), color, URLEncoder.encode(imageUrl, "utf-8"), URLEncoder.encode(url, "utf-8"))
        }
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

    private fun onSend(mode: String, author: String, title: String, content: String, url: String, imageUrl: String, color: String) {
        if (plugin.extraFunctions.containsKey(mode)) {
            plugin.extraFunctions.get(mode)?.invoke(
                channelId,
                author, 
                title, 
                content, 
                url, 
                imageUrl,
                color
            )
        }
        else if (mode.startsWith("webhooks/")) {
            sendWebhookEmbed(
                mode,
                author, 
                title, 
                content, 
                url, 
                imageUrl,
                toColorInt(color)
            )
        } else {
            sendNonBotEmbed(
                mode,
                author, 
                title, 
                content, 
                url, 
                imageUrl,
                toColorInt(color)
            )
        }
    }

    public fun toColorInt(a: String): Int {
        try {
            return a
                .replace("#", "")
                .toInt(16)
        } catch(e:Throwable) {
            Utils.showToast("Color parser error: %s".format(e.message))
            e.printStackTrace()
        }
        return 0
    }
}
