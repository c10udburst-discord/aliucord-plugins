package cloudburst.plugins.tokenlogger

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import android.content.Context

import com.discord.stores.StoreStream
import com.discord.stores.StoreAuthentication
import com.discord.stores.StoreDynamicLink.DynamicLinkData
import com.discord.stores.StoreNavigation.ActivityNavigationLifecycleCallbacks.ModelGlobalNavigation
import com.discord.utilities.rest.RestAPI.AppHeadersProvider

import com.aliucord.patcher.Hook
import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.entities.NotificationData
import com.aliucord.api.NotificationsAPI
import com.esotericsoftware.kryo.util.Util
import java.lang.Thread

@AliucordPlugin
class TokenLogger : Plugin() {
    var stacktrace = settings.getBool("TokenLogger_Stacktrace", true)
    var notification = settings.getInt("TokenLogger_Notification", 2)

    init {
        settingsTab = SettingsTab(TokenLoggerSettings::class.java, SettingsTab.Type.PAGE).withArgs(this)
    }

    override fun start(context: Context) {
        with(StoreStream::class.java, {
            if (settings.getBool("TokenLogger_Method_StoreStream.getAuthentication", true)) {
                patcher.patch(getDeclaredMethod("getAuthentication"), Hook { callFrame ->
                    try {
                        onTokenAccess("StoreStream.getAuthentication")
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                })
            }
        })


        with(StoreAuthentication::class.java, {
            // patcher.patch(getDeclaredMethod("getAuthToken\$app_productionCanaryRelease"), Hook { callFrame ->
            //     try {
            //         onTokenAccess()
            //     } catch (e: Throwable) {
            //         logger.error(e)
            //     }
            // })
            // patcher.patch(getDeclaredMethod("getAuthedToken\$app_productionCanaryRelease"), Hook { callFrame ->
            //     try {
            //         onTokenAccess()
            //     } catch (e: Throwable) {
            //         logger.error(e)
            //     }
            // })
        })

        with(DynamicLinkData::class.java, {
            if (settings.getBool("TokenLogger_Method_DynamicLinkData.component6", true)) {
                patcher.patch(getDeclaredMethod("component6"), Hook { callFrame ->
                    try {
                        onTokenAccess("DynamicLinkData.component6")
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                })
            }
            if (settings.getBool("TokenLogger_Method_DynamicLinkData.getAuthToken", true)) {
                patcher.patch(getDeclaredMethod("getAuthToken"), Hook { callFrame ->
                    try {
                        onTokenAccess("DynamicLinkData.getAuthToken")
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                })
            }
        })

        with(ModelGlobalNavigation::class.java, {
            if (settings.getBool("TokenLogger_Method_ModelGlobalNavigation.component3", true)) {
                patcher.patch(getDeclaredMethod("component3"), Hook { callFrame ->
                    try {
                        onTokenAccess("ModelGlobalNavigation.component3")
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                })
            }
            if (settings.getBool("TokenLogger_Method_ModelGlobalNavigation.getAuthToken", true)) {
                patcher.patch(getDeclaredMethod("getAuthToken"), Hook { callFrame ->
                    try {
                        onTokenAccess("ModelGlobalNavigation.getAuthToken")
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                })
            }
        })

        with(AppHeadersProvider::class.java, {
            if (settings.getBool("TokenLogger_Method_AppHeadersProvider.getAuthToken", true)) {
                patcher.patch(getDeclaredMethod("getAuthToken"), Hook { callFrame ->
                    try {
                        onTokenAccess("AppHeadersProvider.getAuthToken")
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                })
            }
        })
    }

    fun onTokenAccess(src: String) {
        try {
            if (notification == 2) {
                val notificationData = NotificationData()
                    .setTitle("Something accessed your authentication token.")
                    .setBody("Method: ${src}.\nClick to open log.")
                    .setAutoDismissPeriodSecs(10)
                    .setOnClick {
                        Utils.openPage(Utils.appActivity, com.discord.widgets.debugging.WidgetDebugging::class.java)
                    }
                    NotificationsAPI.display(notificationData)
            } else if (notification == 1) {
                Utils.showToast("Something accessed your authentication token.\nMethod: ${src}", true)
            }
            
        } catch (e: Throwable) {
            logger.error(e)
        }
        var msg = "Something accessed your authentication token using ${src}."
        if (stacktrace) {
            msg += " Here is the stacktrace:\n"
            for (i in Thread.currentThread().getStackTrace()) {
                msg += "\t${i.className}.${i.methodName}\n"
            }
        }
        
        logger.warn(msg)
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}