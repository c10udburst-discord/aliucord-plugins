package com.aliucord.plugins

import android.content.Context
import com.discord.api.commands.ApplicationCommandType
import com.discord.models.commands.ApplicationCommandOption
import com.discord.stores.StoreStream
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

import com.aliucord.Utils
import com.aliucord.utils.ReflectUtils
import com.aliucord.plugins.ui.EmbedModal

import androidx.core.content.ContextCompat
import com.lytefast.flexinput.R
import android.view.View
import com.aliucord.patcher.Patcher
import com.aliucord.patcher.PinePatchFn
import com.aliucord.api.SettingsAPI
import top.canyie.pine.Pine.CallFrame
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.lytefast.flexinput.fragment.FlexInputFragment
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageButton
import android.view.ViewGroup.LayoutParams
import android.view.ContextThemeWrapper
import com.discord.utilities.color.ColorCompat
import com.aliucord.plugins.ui.SendEmbedsSettings

@AliucordPlugin
class SendEmbeds : Plugin() {
    
    init {
        settingsTab = SettingsTab(SendEmbedsSettings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(this)
    }

    override fun start(context: Context) {
        val icon = ContextCompat.getDrawable(context, R.d.ic_qr_code_24dp)
        Utils.tintToTheme(icon).setAlpha(0x99);

        commands.registerCommand(
            "embed",
            "Send Embeds",
            emptyList()
        ) { ctx -> 
            EmbedModal(ctx.getChannelId(), settings).show(Utils.appActivity.supportFragmentManager, "SendEmbedModal")
            return@registerCommand null
        }

        with(FlexInputFragment::class.java, { 
            patcher.patch(getDeclaredMethod("onViewCreated", View::class.java, Bundle::class.java), PinePatchFn { callFrame: CallFrame -> 
                try {
                    if (settings.getBool("SendEmbeds_ButtonVisible", true)) {
                        val view = ((callFrame.args[0] as LinearLayout).getChildAt(1) as RelativeLayout).getChildAt(0) as LinearLayout
                    
                        val btn = AppCompatImageButton(ContextThemeWrapper(view.context, R.h.UiKit_ImageView_Clickable), null, 0).apply { 
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                            setImageDrawable(icon)
                            setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundTertiary))
                            setOnClickListener {
                                val channelId = StoreStream.getChannelsSelected().id
                                EmbedModal(channelId, settings).show(Utils.appActivity.supportFragmentManager, "SendEmbedModal")
                            }
                            setPadding(0, 0, 8, 0)
                            setClickable(true)
                        }
                        view.addView(btn)
                    }
                }  catch (ignored: Throwable) {
                    Patcher.logger.error(ignored)
                }
            })
        })
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
    }
}
