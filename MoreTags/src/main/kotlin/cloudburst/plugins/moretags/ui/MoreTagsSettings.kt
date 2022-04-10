package cloudburst.plugins.moretags.ui

import com.aliucord.fragments.SettingsPage
import android.view.View
import android.view.Gravity

import com.aliucord.Utils
import com.discord.views.CheckedSetting
import com.aliucord.widgets.LinearLayout
import com.aliucord.api.SettingsAPI
import com.aliucord.utils.MDUtils
import android.widget.TextView
import com.lytefast.flexinput.R
import cloudburst.plugins.moretags.MoreTags

class MoreTagsSettings(private val plugin: MoreTags) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        plugin.cache.clear()

        setActionBarTitle("MoreTags")
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Cache", "Use cache for displaying role tags, which resets on app restart.").apply {
            val key = "MoreTags_Cache"
            isChecked = plugin.settings.getBool(key, false)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Colorize", "Colorize tags acording to role colors.").apply {
            val key = "MoreTags_Colorize"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Role name", "Use role name instead of permission name").apply {
            val key = "MoreTags_RoleName"
            isChecked = plugin.settings.getBool(key, false)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Webhook", "Rename \"BOT\" to \"WEBHOOK\" or \"SERVER\" when appropriate.").apply {
            val key = "MoreTags_Webhook"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "System", "Rename \"BOT\" to \"SYSTEM\" system messages (eg. Clyde) .").apply {
            val key = "MoreTags_System"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Bot Only", "Wheter to display bots with admin permissions as \"BOT\" or \"BOT • ADMIN\".").apply {
            val key = "MoreTags_BotOnly"
            isChecked = plugin.settings.getBool(key, false)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Owner", "Show \"OWNER\" tags.").apply {
            val key = "MoreTags_Owner"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Administrator", "Show \"ADMIN\" tags.").apply {
            val key = "MoreTags_Admin"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Staff", "Show \"STAFF\" tags.").apply {
            val key = "MoreTags_Staff"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Moderator", "Show \"MOD\" tags.").apply {
            val key = "MoreTags_Mod"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Voice moderator", "Show \"VOICE MOD\" tags.").apply {
            val key = "MoreTags_VoiceMod"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
            }
        })
        addView(TextView(view.context, null, 0, R.i.UiKit_TextView).apply { 
            text = MDUtils.render("""
            
            ***OWNER***: Owner of the server _(duh)_

            ***ADMIN***: Has the administrator permission

            ***STAFF***: Has at least one of the following permissions:
            - Manage Server
            - Manage Channels
            - Manage Roles

            ***MOD***: Has at least one of the following permissions:
            - Kick Members
            - Ban Members
            - Manage Messages

            ***VOICE MOD***: Has at least one of the following permissions:
            - Mute Members
            - Deafen Members
            - Move Members

            ***SERVER***: Message that was crossposted from another server

            ***WEBHOOK***: Message that was sent by a webhook

            ***SYSTEM***: Sender is a system user (eg. Clyde)
            """.trimIndent())
        })
    }
}
