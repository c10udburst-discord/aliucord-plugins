package cloudburst.plugins.linkreplace.ui

import com.aliucord.fragments.SettingsPage
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cloudburst.plugins.linkreplace.LinkReplace
import cloudburst.plugins.linkreplace.utils.LinkReplacement
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import com.aliucord.utils.DimenUtils
import com.aliucord.Utils
import android.graphics.Color
import com.aliucord.views.Button

class ReplacerSettings : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val ctx = view.context
        setActionBarTitle("LinkReplace")

        val replacementRules = LinkReplace.mSettings.getObject("LinkReplace_Rules", LinkReplacement.DEFAULT_LIST, Array<LinkReplacement>::class.java).toMutableList()
        
        val recycler = RecyclerView(ctx).apply {
            adapter = ReplacerAdapter(this@ReplacerSettings, replacementRules)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

            val decoration = DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
            ShapeDrawable(RectShape()).run {
                setTint(Color.TRANSPARENT)
                intrinsicHeight = DimenUtils.getDefaultPadding()
                decoration.setDrawable(this)
            }
            addItemDecoration(decoration)
        }
        addView(recycler)

        Button(ctx).run {
            text = "New Rule"
            DimenUtils.getDefaultPadding().let {
                setPadding(it, it, it, it)
            }
            setOnClickListener {
                replacementRules.add(LinkReplacement("", ""))
                recycler.adapter!!.notifyItemInserted(replacementRules.size-1)
            }
            linearLayout.addView(this)
        }

        setOnBackPressed {
            replacementRules.removeIf { 
                it.fromText == "" || it.toDomain == ""
            }

            LinkReplace.mSettings.setObject("LinkReplace_Rules", replacementRules.toTypedArray())
            false
        }
    }

}