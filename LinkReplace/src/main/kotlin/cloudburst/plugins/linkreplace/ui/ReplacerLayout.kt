package cloudburst.plugins.linkreplace.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.fragments.SettingsPage
import cloudburst.plugins.linkreplace.utils.LinkReplacement
import android.text.TextWatcher
import android.text.Editable

class ReplacerAdapter(private val settings: SettingsPage, private val rules: List<LinkReplacement>) : RecyclerView.Adapter<ReplacerHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReplacerHolder(this, ReplacerCard(parent.context))
    
    override fun onBindViewHolder(holder: ReplacerHolder, position: Int) = rules.elementAt(position).let {
        holder.card.run { 
            regex.editText!!.apply { 
                setText(it.fromText)
                addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(s: Editable) { }
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
                    override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                        it.fromText = s.toString()
                    }
                })
            }
            toDomain.editText!!.run { 
                setText(it.toDomain)
                addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(s: Editable) { }
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
                    override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                        it.toDomain = s.toString()
                    }
                })
            }
        }
    }

    override fun getItemCount() = rules.size
}

class ReplacerHolder(private val adapter: ReplacerAdapter, val card: ReplacerCard) : RecyclerView.ViewHolder(card) {

}