package cloudburst.plugins.sendembeds.ui

import cloudburst.plugins.sendembeds.SendEmbeds
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
        layout.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))

        val title = TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply { 
            text = "SendEmbeds settings"
        }
        layout.addView(title)

        layout.addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show button", null).apply {
            val key = "SendEmbeds_ButtonVisible"
            isChecked = plugin.settings.getBool(key, false)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        layout.addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Hide embed link", "Sends links with hidden glitch. Note: Only hides on web/pc").apply {
            val key = "SendEmbeds_LinkVisible"
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
