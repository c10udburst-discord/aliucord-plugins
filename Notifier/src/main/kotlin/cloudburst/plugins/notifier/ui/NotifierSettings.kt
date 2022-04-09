package cloudburst.plugins.notifier.ui

import cloudburst.plugins.notifier.Notifier
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

class NotifierSettings(private val plugin: Notifier) : AppBottomSheet() {
    override fun getContentViewResId() = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View {
        val context = inflater.context
        val layout = LinearLayout(context)
        layout.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))

        val title = TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply { 
            text = "Notifier settings"
        }
        layout.addView(title)

        layout.addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Guild leaves", null).apply {
            val key = "Notifier_GuildLeaves"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        layout.addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Friend removals", null).apply {
            val key = "Notifier_FriendRemovals"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        return layout
    }
}
