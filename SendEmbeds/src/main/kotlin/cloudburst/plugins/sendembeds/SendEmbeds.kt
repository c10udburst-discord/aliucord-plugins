package cloudburst.plugins.sendembeds

import android.content.Context
import com.discord.api.commands.ApplicationCommandType
import com.discord.models.commands.ApplicationCommandOption
import com.discord.stores.StoreStream
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

import com.aliucord.Utils
import com.aliucord.utils.ReflectUtils
import cloudburst.plugins.sendembeds.ui.EmbedModal

import androidx.core.content.ContextCompat
import com.lytefast.flexinput.R
import android.view.View
import com.aliucord.patcher.Patcher
import com.aliucord.patcher.Hook
import com.aliucord.api.SettingsAPI
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.lytefast.flexinput.fragment.FlexInputFragment
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageButton
import android.view.ViewGroup.LayoutParams
import android.view.ContextThemeWrapper
import android.app.Activity
import com.discord.utilities.color.ColorCompat
import cloudburst.plugins.sendembeds.ui.SendEmbedsSettings
import com.aliucord.fragments.AppFragmentProxy

@AliucordPlugin
class SendEmbeds : Plugin() {
    
    //                                         channel,  author, title,  content, url,    imageUrl, color
    public val extraFunctions = hashMapOf<String, (Long,   String, String, String,  String, String,   String) -> Unit>()
    public val modes = mutableListOf(
        "embed.rauf.workers.dev",
        "embed.rauf.wtf",
        "rauf.wtf/embed"
    )

    init {
        settingsTab = SettingsTab(SendEmbedsSettings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(this)
    }

    override fun start(context: Context) {
        val icon = ContextCompat.getDrawable(context, R.d.ic_embed_white_24dp)
        var fragmentManager = Utils.appActivity.supportFragmentManager
        Utils.tintToTheme(icon)?.setAlpha(0x99);

        commands.registerCommand(
            "embed",
            "Send Embeds",
            emptyList()
        ) { ctx -> 
            EmbedModal(ctx.getChannelId(), this).show(fragmentManager, "SendEmbeds")
            return@registerCommand null
        }

        with(FlexInputFragment::class.java, { 
            patcher.patch(getDeclaredMethod("onViewCreated", View::class.java, Bundle::class.java), Hook { callFrame -> 
                try {
                    fragmentManager = (callFrame.thisObject as FlexInputFragment).parentFragmentManager
                    if (settings.getBool("SendEmbeds_ButtonVisible", false)) {
                        
                        val view = ((callFrame.args[0] as LinearLayout).getChildAt(1) as RelativeLayout).getChildAt(0) as LinearLayout
                    
                        val btn = AppCompatImageButton(ContextThemeWrapper(view.context, R.h.UiKit_ImageView_Clickable), null, 0).apply { 
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                            setImageDrawable(icon)
                            setBackgroundColor(0)
                            setOnClickListener {
                                val channelId = StoreStream.getChannelsSelected().id
                                EmbedModal(channelId, this@SendEmbeds).show(fragmentManager, "SendEmbeds")
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
        patcher.unpatchAll()
    }
}
