package com.aliucord.plugins.ui

import com.aliucord.plugins.SendEmbeds
import com.discord.app.AppBottomSheet
import com.aliucord.widgets.LinearLayout
import android.content.Context
import android.os.Bundle
import android.view.*
import com.discord.utilities.color.ColorCompat
import com.aliucord.Utils
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R
import android.widget.TextView

class SendEmbedsSettings(private val plugin: SendEmbeds) : AppBottomSheet() {
    override fun getContentViewResId() = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View {
        val context = inflater.context
        val layout = LinearLayout(context)
        var count = 0
        layout.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))

        val title = TextView(context, null, 0, R.h.UiKit_Settings_Item_Header).apply { 
            text = "SendEmbeds settings"
            setOnClickListener {
                count++
                plugin.settings.setBool("SendEmbeds_SelfBotMode", count==7) // shh dont tell anyone
                if (count == 7)
                    Utils.showToast("Uncaught exception on main thread")
            }
        }
        layout.addView(title)

        layout.addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show button", null).apply {
            val key = "SendEmbeds_ButtonVisible"
            isChecked = plugin.settings.getBool(key, false)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        layout.addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "NQN compatibility", "Sends links with markdown syntax, so NQN will convert them to webhooks without links visible").apply {
            val key = "SendEmbeds_NQNCompatibility"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        return layout
    }
}
