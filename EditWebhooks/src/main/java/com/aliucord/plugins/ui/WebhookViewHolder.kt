package com.aliucord.plugins.ui

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.facebook.drawee.view.SimpleDraweeView

class ViewHolder(private val adapter: WebhookRecyclerAdapter, layout: ConstraintLayout) : RecyclerView.ViewHolder(layout), View.OnClickListener {
    private val iconId = Utils.getResId("user_profile_adapter_item_server_image", "id")
    private val iconTextId = Utils.getResId("user_profile_adapter_item_server_text", "id")
    private val serverNameId = Utils.getResId("user_profile_adapter_item_server_name", "id")
    private val displayNameId = Utils.getResId("user_profile_adapter_item_user_display_name", "id")
    private val guildAvatarId = Utils.getResId("guild_member_avatar", "id")

    val icon = layout.findViewById(iconId) as SimpleDraweeView
    val name = layout.findViewById(serverNameId) as TextView
    val subTitle = layout.findViewById(displayNameId) as TextView

    init {
        layout.findViewById<View>(iconTextId).visibility = View.GONE
        layout.findViewById<View>(guildAvatarId).visibility = View.GONE
        layout.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        adapter.onClick(adapterPosition)
    }
}