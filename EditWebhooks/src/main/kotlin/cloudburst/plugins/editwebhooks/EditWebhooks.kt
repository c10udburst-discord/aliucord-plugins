package cloudburst.plugins.editwebhooks

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.fragments.InputDialog
import com.aliucord.patcher.Hook
import androidx.core.content.res.ResourcesCompat
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.channels.settings.WidgetTextChannelSettings
import com.discord.databinding.WidgetTextChannelSettingsBinding
import com.lytefast.flexinput.R
import java.lang.reflect.InvocationTargetException
import com.aliucord.utils.ReflectUtils
import com.aliucord.Constants
import com.discord.stores.StoreStream
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.Utils
import com.aliucord.utils.GsonUtils
import com.aliucord.Http
import com.google.gson.JsonObject
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.analytics.AnalyticSuperProperties
import com.aliucord.patcher.Patcher
import cloudburst.plugins.editwebhooks.ui.WebhookList

@AliucordPlugin
class EditWebhooks : Plugin() {
    @SuppressLint("SetTextI18n")
    override fun start(context: Context) {
        val iconLeft = ContextCompat.getDrawable(context, R.e.ic_publish_24dp)
        val iconRight = ContextCompat.getDrawable(context, R.e.icon_carrot)
        val editWebooksId = View.generateViewId()

        with(WidgetTextChannelSettings::class.java, {
            val getBinding = getDeclaredMethod("getBinding").apply { isAccessible = true }

            patcher.patch(getDeclaredMethod("configureUI", WidgetTextChannelSettings.Model::class.java), Hook { callFrame ->
                try {
                    val channel = (callFrame.args[0] as WidgetTextChannelSettings.Model).channel


                    val binding = getBinding.invoke(callFrame.thisObject) as WidgetTextChannelSettingsBinding
                    
                    binding.a.findViewById<TextView>(editWebooksId).apply { 
                        visibility = View.VISIBLE
                        setOnClickListener {
                            val list = WebhookList(channel)
                            Utils.openPageWithProxy(Utils.appActivity, list)
                        }
                        isClickable = true
                    }
                } catch (ignored: Throwable) {
                    logger.error(ignored)
                }
            })

            patcher.patch(getDeclaredMethod("onViewBound", View::class.java), Hook { callFrame ->
                val coordinatorLayout = (callFrame.args[0] as CoordinatorLayout)
                val nestedScrollView = coordinatorLayout.getChildAt(1) as NestedScrollView
                val linearLayout = nestedScrollView.findViewById(Utils.getResId("channel_settings_section_privacy_safety", "id")) as LinearLayout
                val ctx = linearLayout.context

                iconLeft?.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))
                iconRight?.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))

                val editWebhooks = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                    text = "Edit Webhooks"
                    id = editWebooksId
                    setCompoundDrawablesRelativeWithIntrinsicBounds(iconLeft, null, iconRight, null)
                    typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
                }

                linearLayout.addView(editWebhooks)
            })
        })
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
