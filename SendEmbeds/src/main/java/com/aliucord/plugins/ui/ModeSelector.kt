package com.aliucord.plugins.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.widgets.LinearLayout
import com.discord.app.AppBottomSheet
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.discord.views.RadioManager
import android.content.DialogInterface
import com.lytefast.flexinput.R

class ModeSelector(val radioNames: List<String>, val onClose: (String) -> Unit) : AppBottomSheet() {
    var mode = "embed.rauf.workers.dev" 

    override fun getContentViewResId() = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context

        val layout = LinearLayout(context).apply {
            setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))
        }

        val radios = radioNames.map {
            Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, it, null)
        }

        val radioManager = RadioManager(radios)
        val radioSize = radios.size
        for (i in 0 until radioSize) {
            val radio = radios[i]
            radio.e {
                radioManager.a(radio)
                mode = radioNames[i]
            }
            layout.addView(radio)
        }
        return layout
    }

    override fun onDismiss(dialogInteface: DialogInterface) {
        onClose.invoke(mode)
        super.onDismiss(dialogInteface)
    }
}