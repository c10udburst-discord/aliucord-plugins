package cloudburst.plugins.moretags

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.PreHook
import com.aliucord.patcher.Patcher
import com.aliucord.Utils
import android.content.Context
import androidx.core.content.ContextCompat
import android.view.View

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.models.message.Message
import com.discord.models.member.GuildMember
import com.aliucord.wrappers.ChannelWrapper
import com.aliucord.utils.ReflectUtils
import com.aliucord.wrappers.GuildMemberWrapper
import com.discord.api.user.User
import com.discord.stores.StoreStream
import com.discord.utilities.permissions.PermissionUtils
import android.widget.TextView
import android.graphics.Color
import com.discord.api.permission.*
import com.lytefast.flexinput.R
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.channels.memberlist.adapter.*
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.discord.databinding.UserProfileHeaderViewBinding

import cloudburst.plugins.moretags.ui.MoreTagsSettings

@AliucordPlugin
class MoreTags : Plugin() {

    //val cache: HashMap<Pair<Long, Long>, String?> = hashMapOf();
    init {
        settingsTab = SettingsTab(MoreTagsSettings::class.java, SettingsTab.Type.PAGE).withArgs(this.settings)
    }

    override fun start(context: Context) {
        with(WidgetChatListAdapterItemMessage::class.java) { // in chat
            patcher.patch(getDeclaredMethod("configureItemTag", Message::class.java), Hook { callFrame -> try {
                
                val tag = getDeclaredField("itemTag").let {
                    it.isAccessible = true
                    it.get(callFrame.thisObject) as TextView?
                }
                if (tag == null) return@Hook

                val msg = callFrame.args[0] as Message

                if (msg.author.f() == "0000") {
                    setTag(context, tag, 
                        if (settings.getBool("MoreTags_Webhook", true) && msg.webhookId != null)
                            (if (msg.isCrosspost()) "SERVER" else "WEBHOOK")
                        else if (settings.getBool("MoreTags_System", true) && msg.author.i() == -1L)
                            "SYSTEM"
                        else "BOT"
                    , null)
                    return@Hook
                }

                val channel = ChannelWrapper(StoreStream.getChannels().getChannel(msg.channelId))
                if (channel.guildId == null) {
                    setTag(context, tag, if (msg.author.e() == true) "BOT" else "", null)
                } else {
                    val member = StoreStream.getGuilds().getMember(channel.guildId, msg.author.i())
                    if (member == null) return@Hook;

                    val tagStr = getTag(channel.guildId, member)
                    setTag(context, tag, 
                        if (msg.author.e() == true && (settings.getBool("MoreTags_BotOnly", false) || tagStr == null)) "BOT"
                        else if (msg.author.e() == true) "BOT • ${tagStr}"
                        else tagStr ?: "",
                    member.color)
                }
            
                

            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }

        with(ChannelMembersListViewHolderMember::class.java) { // in member list
            patcher.patch(getDeclaredMethod("bind", ChannelMembersListAdapter.Item.Member::class.java, Function0::class.java), Hook { callFrame -> try {
                val layout = (ReflectUtils.getField(callFrame.thisObject, "binding") as WidgetChannelMembersListItemUserBinding).root
                val user = callFrame.args[0] as ChannelMembersListAdapter.Item.Member
                val tag = layout.findViewById(Utils.getResId("username_tag", "id")) as TextView

                val guildId = user.guildId
                if (guildId == null) {
                    setTag(context, tag, if (user.isBot() == true) "BOT" else "", null)
                } else {
                    val member = StoreStream.getGuilds().getMember(guildId, user.userId)
                    if (member == null) return@Hook;

                    val tagStr = getTag(guildId, member)
                    setTag(context, tag, 
                        if (user.isBot() == true && (settings.getBool("MoreTags_BotOnly", false) || tagStr == null)) "BOT"
                        else if (user.isBot() == true) "BOT • ${tagStr}"
                        else tagStr ?: "",
                    member.color)
                }
                
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }

        with(UserProfileHeaderView::class.java) { 
            patcher.patch(getDeclaredMethod("updateViewState", UserProfileHeaderViewModel.ViewState.Loaded::class.java), Hook { callFrame -> try {
                val layout = (ReflectUtils.getField(callFrame.thisObject, "binding") as UserProfileHeaderViewBinding).a
                val state = callFrame.args[0] as UserProfileHeaderViewModel.ViewState.Loaded
                val member = state.guildMember
                val user = state.user
                val tag = layout.findViewById(Utils.getResId("username_tag", "id")) as TextView

                if (member == null) return@Hook;

                val tagStr = getTag(member.guildId, member)
                setTag(context, tag, 
                    if (user.isBot() == true && (settings.getBool("MoreTags_BotOnly", false) || tagStr == null)) "BOT"
                    else if (user.isBot() == true) "BOT • ${tagStr}"
                    else tagStr ?: "",
                member.color)
                
            } catch (ignored: Throwable) {
                logger.error(ignored)
            }})
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun setTag(context: Context, tag: TextView, newText: String, color: Int?) {
        val bgColor = if (settings.getBool("MoreTags_Colorize", true) && color != null)
            color
        else
            ColorCompat.getColor(context, R.c.brand_new_500)
        val fgColor = contrastColor(bgColor)
        tag.apply { 
            text = newText
            visibility = if (newText == "") View.GONE else View.VISIBLE
            background.setTint(bgColor)
            setTextColor(fgColor)
            getCompoundDrawables().let {
                it.set(0, it[0]?.mutate())
                it[0]?.setVisible(false, true)
                it[0]?.setTint(fgColor)
            }
        }
    }

    private fun contrastColor(color: Int): Int {
        val c = Color.valueOf(color)
        if ((c.red()*54.213 + c.green()*182.376 + c.blue()*18.411) >= 140) return Color.BLACK.toInt()
        else return Color.WHITE.toInt()
    }

    private fun getTag(guildId: Long, member: GuildMember): String? {
        val checkOwner = settings.getBool("MoreTags_Owner", true)
        val checkAdmin = settings.getBool("MoreTags_Admin", true)
        val checkStaff = settings.getBool("MoreTags_Staff", true)
        val checkMod = settings.getBool("MoreTags_Mod", true)
        if (!(checkOwner || checkAdmin || checkStaff || checkMod)) return null

        val guild = StoreStream.getGuilds().getGuild(guildId)
        if (guild == null) return null

        if (checkOwner && guild.isOwner(member.userId)) return "OWNER"
        val roleList = StoreStream.getGuilds().roles.get(guildId)
        if (roleList == null) return null;

        var isAdmin = false
        var isMod = false
        var isStaff = false
        for (roleId in member.roles) {
            val role = roleList.get(roleId)
            if (role == null) continue
            val perms = role.h()
            if (checkAdmin && PermissionUtils.can(Permission.ADMINISTRATOR, perms)) {
                isAdmin = true
                break
            }
            if (PermissionUtils.can(Permission.MANAGEMENT_PERMISSIONS, perms) 
                || PermissionUtils.can(Permission.MANAGE_CHANNELS, perms) 
                || PermissionUtils.can(Permission.MANAGE_ROLES, perms)) {
                isStaff = true
            }
            if (PermissionUtils.can(Permission.KICK_MEMBERS, perms) 
                || PermissionUtils.can(Permission.BAN_MEMBERS, perms) 
                || PermissionUtils.can(Permission.MANAGE_MESSAGES, perms)) {
                isMod = true
            }
            
        }
        
        if (checkAdmin && isAdmin) return "ADMIN"
        else if (checkStaff && isStaff) return "STAFF"
        else if (checkMod && isMod) return "MOD"

        return null
    }
}
