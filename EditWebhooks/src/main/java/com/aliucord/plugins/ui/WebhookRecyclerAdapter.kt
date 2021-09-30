package com.aliucord.plugins.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.lytefast.flexinput.R
import com.aliucord.plugins.utils.Webhook
import androidx.fragment.app.FragmentManager


class WebhookRecyclerAdapter(
    private val webhooks: List<Webhook>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = ViewHolder(LinearLayout(parent.context))

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val itemView = holder.itemView as LinearLayout
        itemView.addView(WebhookCard(itemView.context,  webhooks[position], fragmentManager))
    }

    override fun getItemCount(): Int {
        return webhooks.size
    }

}