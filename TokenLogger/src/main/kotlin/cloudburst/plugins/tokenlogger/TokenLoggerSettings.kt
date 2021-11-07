package cloudburst.plugins.tokenlogger

import com.aliucord.fragments.SettingsPage
import android.view.View
import com.aliucord.Utils
import com.discord.views.CheckedSetting
import com.aliucord.views.Divider
import android.widget.TextView
import com.lytefast.flexinput.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import androidx.fragment.app.Fragment
import android.content.Intent
import kotlin.system.exitProcess
import com.discord.views.RadioManager

class TokenLoggerSettings(private val plugin: TokenLogger) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("TokenLogger")
    
        val ctx = view.context

        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "Show stacktrace", "Display which functions executed. Can be resource-heavy").apply {
            val key = "TokenLogger_Stacktrace"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
                plugin.stacktrace = it
            }
        })
        arrayListOf(
            Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.RADIO, "None", "Don't notify, just log"),
            Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.RADIO, "Toast", "Show toast"),
            Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.RADIO, "Notification", "Show notification"),
        ).let { radios ->
            val key = "TokenLogger_Notification"
            val manager = RadioManager(radios)
            manager.a(radios[plugin.settings.getInt(key, 2)])
            for (i in 0 until radios.size) {
                val radio = radios[i]
                radio.e {
                    manager.a(radio)
                    plugin.settings.setInt(key, i)
                    plugin.notification = i
                }
                addView(radio)
            }
        }
        

        addView(Divider(ctx))

        addView(TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply { 
            text = "Methods"
        })
        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "StoreStream.getAuthentication", null).apply {
            val key = "TokenLogger_Method_StoreStream.getAuthentication"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
                promptRestart(view, this@TokenLoggerSettings)
            }
        })
        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "DynamicLinkData.component6", null).apply {
            val key = "TokenLogger_Method_DynamicLinkData.component6"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
                promptRestart(view, this@TokenLoggerSettings)
            }
        })
        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "DynamicLinkData.getAuthToken", null).apply {
            val key = "TokenLogger_Method_DynamicLinkData.getAuthToken"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
                promptRestart(view, this@TokenLoggerSettings)
            }
        })
        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "ModelGlobalNavigation.component3", null).apply {
            val key = "TokenLogger_Method_ModelGlobalNavigation.component3"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
                promptRestart(view, this@TokenLoggerSettings)
            }
        })
        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "ModelGlobalNavigation.getAuthToken", null).apply {
            val key = "TokenLogger_Method_ModelGlobalNavigation.getAuthToken"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
                promptRestart(view, this@TokenLoggerSettings)
            }
        })
        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "AppHeadersProvider.getAuthToken", null).apply {
            val key = "TokenLogger_Method_AppHeadersProvider.getAuthToken"
            isChecked = plugin.settings.getBool(key, true)
            setOnCheckedListener {
                plugin.settings.setBool(key, it)
                promptRestart(view, this@TokenLoggerSettings)
            }
        })
    }

    companion object {
        fun promptRestart(v: View, fragment: Fragment, msg: String = "Changes detected. Restart?") {
            Snackbar.make(v, msg, LENGTH_INDEFINITE)
                .setAction("Restart") {
                    val ctx = it.context
                    ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.run {
                        fragment.startActivity(Intent.makeRestartActivityTask(component))
                        exitProcess(0)
                    }
                }.show()
        }
    }
}