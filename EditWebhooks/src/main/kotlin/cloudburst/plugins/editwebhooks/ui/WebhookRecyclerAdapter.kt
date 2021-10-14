package cloudburst.plugins.editwebhooks.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.lytefast.flexinput.R
import cloudburst.plugins.editwebhooks.utils.Webhook
import com.aliucord.Utils
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.color.ColorCompat
import cloudburst.plugins.editwebhooks.ui.WebhookList

class WebhookRecyclerAdapter(
    private val webhooks: List<Webhook>,
    private val parent: WebhookList
) : RecyclerView.Adapter<ViewHolder>() {

    private val layoutId = Utils.getResId("widget_user_profile_adapter_item_server", "layout")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        layout.setBackgroundColor(ColorCompat.getThemedColor(parent.context, R.b.colorBackgroundSecondary))
        layout.setClickable(true)
        return ViewHolder(this, layout as ConstraintLayout)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = webhooks.elementAt(position).let {
        holder.name.text = it.name
        holder.subTitle.text = it.typeReadable
        IconUtils.setIcon(holder.icon, it.avatarUrl+"?size=128")
    }

    override fun getItemCount(): Int {
        return webhooks.size
    }

    fun onClick(position: Int) = webhooks.elementAt(position).let {
        WebhookMenu(it, parent).show(parent.parentFragmentManager, it.name)
    }
}