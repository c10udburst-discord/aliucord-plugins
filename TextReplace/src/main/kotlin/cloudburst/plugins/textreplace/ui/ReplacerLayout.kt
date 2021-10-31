package cloudburst.plugins.textreplace.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.fragments.SettingsPage
import cloudburst.plugins.textreplace.utils.TextReplacement
import android.text.TextWatcher
import android.text.Editable

class ReplacerAdapter(private val settings: SettingsPage, private val rules: List<TextReplacement>) : RecyclerView.Adapter<ReplacerHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReplacerHolder(this, ReplacerCard(parent.context))
    
    override fun onBindViewHolder(holder: ReplacerHolder, position: Int) = rules.elementAt(position).let {
        holder.card.apply(it)
    }

    override fun getItemCount() = rules.size
}

class ReplacerHolder(private val adapter: ReplacerAdapter, val card: ReplacerCard) : RecyclerView.ViewHolder(card) {

}