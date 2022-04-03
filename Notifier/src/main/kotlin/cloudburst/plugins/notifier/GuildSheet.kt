package cloudburst.plugins.notifier

import com.discord.app.AppBottomSheet
import com.discord.models.guild.Guild
import com.discord.app.AppViewFlipper
import com.discord.utilities.color.ColorCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.button.MaterialButton

import com.aliucord.Utils
import com.aliucord.views.Button
import com.lytefast.flexinput.R

import android.view.*
import android.widget.LinearLayout
import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.discord.stores.StoreInviteSettings

import com.discord.widgets.guilds.invite.WidgetGuildInvite;

class GuildSheet(val guild : Guild) : AppBottomSheet() {
    override fun getContentViewResId() = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View {
        val view = inflater.inflate(Utils.getResId("widget_guild_profile_sheet", "layout"), container, false)
        view.visibility = View.VISIBLE
        val flipper = view.findViewById<View>(Utils.getResId("guild_profile_sheet_flipper", "id")) as AppViewFlipper
        flipper.displayedChild = 1

        val banner = view.findViewById<View>(Utils.getResId("guild_profile_sheet_banner", "id")) as SimpleDraweeView?
        banner?.setImageURI("https://cdn.discordapp.com/banners/${guild.id}/${guild.banner}.jpg?size=512")

        val icon = view.findViewById<View>(Utils.getResId("guild_profile_sheet_icon", "id")) as SimpleDraweeView?
        icon?.setImageURI("https://cdn.discordapp.com/icons/${guild.id}/${guild.icon}.png")

        val name = view.findViewById<View>(Utils.getResId("guild_profile_sheet_name", "id")) as TextView?
        name?.setText(guild.name)

        val description = view.findViewById<View>(Utils.getResId("guild_profile_sheet_description", "id")) as TextView?
        description?.setText(guild.description)

        val onlineCount = view.findViewById<View>(Utils.getResId("guild_profile_sheet_online_count", "id")) as LinearLayout?
        onlineCount?.visibility = View.GONE

        val memberCount = view.findViewById<View>(Utils.getResId("guild_profile_sheet_member_count", "id")) as LinearLayout?
        memberCount?.visibility = View.GONE

        val bottomSheet = view.findViewById<View>(Utils.getResId("guild_profile_sheet_tab_items", "id")) as LinearLayout?
        bottomSheet?.findViewById<View>(Utils.getResId("guild_profile_sheet_boosts", "id"))?.apply {
            visibility = View.GONE
        }
        bottomSheet?.findViewById<MaterialButton>(Utils.getResId("guild_profile_sheet_notifications", "id"))?.apply {
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(context, R.e.ic_copy_24dp), null, null)
            setText("Copy ID")
            setOnClickListener {
                Utils.setClipboard("id", guild.id.toString())
                Utils.showToast("Copied to clipboard", false)
            }
        
        }
        bottomSheet?.findViewById<View>(Utils.getResId("guild_profile_sheet_settings", "id"))?.apply {
            visibility = View.GONE
        }
        bottomSheet?.findViewById<View>(Utils.getResId("guild_profile_sheet_invite", "id"))?.apply {
            if (guild.vanityUrlCode != null) {
                visibility = View.VISIBLE
                setOnClickListener {
                    WidgetGuildInvite.Companion(null).launch(context, StoreInviteSettings.InviteCode(guild.vanityUrlCode, "", null))
                }
            } else {
                visibility = View.GONE
            }
        }


        return view
    }
}