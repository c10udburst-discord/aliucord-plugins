package cloudburst.plugins.notifier

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PreHook
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.aliucord.entities.NotificationData
import com.aliucord.Utils
import com.aliucord.api.NotificationsAPI

import android.content.Context
import android.view.View
import android.os.Bundle

import com.discord.stores.StoreGuilds
import com.discord.stores.StoreUserRelationships
import com.discord.stores.StoreStream

import com.discord.api.guild.Guild
import com.discord.models.domain.ModelUserRelationship
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.lytefast.flexinput.fragment.FlexInputFragment
import com.discord.widgets.guilds.profile.WidgetGuildProfileSheet

@AliucordPlugin
class Notifier : Plugin() {

    var fragmentManager = Utils.appActivity.supportFragmentManager

    override fun start(context: Context) {
        with(FlexInputFragment::class.java) {  // this is only here because Utils.appActivity.supportFragmentManager is scuffed
            patcher.patch(getDeclaredMethod("onViewCreated", View::class.java, Bundle::class.java), Hook { callFrame -> 
                try {
                    fragmentManager = (callFrame.thisObject as FlexInputFragment).parentFragmentManager
                } catch (ignored: Exception) {
                    logger.error(ignored)
                }
            })
        }

        with(StoreGuilds::class.java) {
            patcher.patch(getDeclaredMethod("handleGuildRemove", Guild::class.java), PreHook { callFrame -> try {
                onGuildRemoval(callFrame.args[0] as Guild)
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }
        with(StoreUserRelationships::class.java) {
            patcher.patch(getDeclaredMethod("handleRelationshipRemove", ModelUserRelationship::class.java), PreHook { callFrame -> try {
                logger.info("Relationship removed ${callFrame.args[0]}")
                if (callFrame.args[0] is ModelUserRelationship) {
                    onUnfriend((callFrame.args[0] as ModelUserRelationship).id)
                } else {
                    onUnfriend(null)
                }
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun onGuildRemoval(guild: Guild?) {
        NotificationsAPI.display(if (guild == null || guild.r() == 0L || guild.r() == -1L) {
            logger.info("Unknown guild removal")
            NotificationData().apply { 
                title = "Unknown guild was removed"
                body = "You have been kicked/banned from a guild or it was removed"
            }
        } else {
            val id = guild.r()
            val cachedGuild = StoreStream.getGuilds().getGuild(guild.r())
            if (cachedGuild == null) {
                logger.info("Guild removal: ${id}")
                NotificationData().apply {
                    title = "Guild $id was removed"
                    body = "You have been kicked/banned from a guild or it was removed.\nClick to copy id"
                    setOnClick {
                        Utils.setClipboard("id", id.toString())
                        Utils.showToast("Copied to clipboard", false)
                    }
                }
            } else {
                logger.info("Guild removal: ${cachedGuild.toString()}")
                NotificationData().apply {
                    iconUrl = "https://cdn.discordapp.com/icons/$id/${cachedGuild.icon}.png"
                    title = "Guild \"${cachedGuild.name}\" was removed"
                    body = "You have been kicked/banned from a guild or it was removed.\nClick to see details"
                    setOnClick {
                        try {
                            GuildSheet(cachedGuild).show(fragmentManager, "GuildSheet")
                        } catch (ignored: Throwable) {
                            logger.error(ignored)
                        }
                    }
                }
            }
            
        })
    }

    private fun onUnfriend(id: Long?) {
        NotificationsAPI.display(if (id == null) {
            NotificationData().apply { 
                title = "Unknown user unfriended you"
                body = "You have been unfriended"
            }
        } else {
            val user = StoreStream.getUsers().getUsers(arrayListOf(id), true).values.firstOrNull()
            if (user != null) {
                NotificationData().apply { 
                    iconUrl = "https://cdn.discordapp.com/avatars/${id}/${user.avatar}.png"
                    title = "User ${user.username} unfriended you"
                    body = "You have been unfriended\nClick to open user sheet"
                    setOnClick {
                        try {
                            WidgetUserSheet.Companion(null).show(id, fragmentManager)
                        } catch (ignored: Throwable) {
                            logger.error(ignored)
                        }
                    }
                }
            } else {
                NotificationData().apply { 
                    title = "User ${id} unfriended you"
                    body = "You have been unfriended\nClick to open user sheet"
                    setOnClick {
                        try {
                            WidgetUserSheet.Companion(null).show(id, fragmentManager)
                        } catch (ignored: Throwable) {
                            logger.error(ignored)
                        }
                    }
                }
            }
        })
    }
}
