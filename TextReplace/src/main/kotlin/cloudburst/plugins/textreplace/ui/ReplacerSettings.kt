package cloudburst.plugins.textreplace.ui

import com.aliucord.fragments.SettingsPage
import android.view.View
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cloudburst.plugins.textreplace.TextReplace
import cloudburst.plugins.textreplace.utils.TextReplacement
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.GsonUtils
import com.aliucord.Utils
import android.graphics.Color
import com.aliucord.views.Button
import com.aliucord.views.ToolbarButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.lytefast.flexinput.R
import com.aliucord.fragments.InputDialog

class ReplacerSettings : SettingsPage() {
    val headerId = View.generateViewId()
    var replacementRules = TextReplace.replacementRules.toMutableList()

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        val ctx = view.context
        setActionBarTitle("TextReplace")
        
        val recycler = RecyclerView(ctx).apply {
            adapter = ReplacerAdapter(this@ReplacerSettings, replacementRules)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

            val decoration = DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
            ShapeDrawable(RectShape()).run {
                setTint(Color.TRANSPARENT)
                intrinsicHeight = DimenUtils.defaultPadding
                decoration.setDrawable(this)
            }
            addItemDecoration(decoration)
        }
        addView(recycler)

        Button(ctx).run {
            text = "New Rule"
            DimenUtils.defaultPadding.let {
                setPadding(it, it, it, it)
            }
            setOnClickListener {
                replacementRules.add(TextReplacement.emptyRule())
                recycler.adapter!!.notifyItemInserted(replacementRules.size-1)
            }
            linearLayout.addView(this)
        }

        setOnBackPressed {
            try {
                updateRules(recycler)
                TextReplace.mSettings.setObject("TextReplace_Rules", replacementRules.toTypedArray())
                TextReplace.replacementRules = replacementRules.toTypedArray()
            } catch (e: Throwable) {
                Utils.showToast(e.toString())
                return@setOnBackPressed true
            }
            return@setOnBackPressed false
        }

        if (headerBar.findViewById<View>(headerId) == null) {
            val p = (DimenUtils.defaultPadding / 2)

            val exportBtn = ToolbarButton(ctx)
            exportBtn.setId(headerId)
            val importBtn = ToolbarButton(ctx)

            exportBtn.layoutParams = Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.END
            }
            importBtn.layoutParams = Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.END
                setMarginEnd(p)
            }

            exportBtn.setOnClickListener {
                updateRules(recycler)
                Utils.setClipboard("TextReplaceRules", GsonUtils.toJson(replacementRules))
                Utils.showToast("Rules copied to clipboard")
            }
            importBtn.setOnClickListener {
                val inDialog = InputDialog()
                                .setTitle("Import rules")
                                .setDescription("Paste previously exported rules here")
                                .setPlaceholderText("Rules JSON")
                                inDialog.setOnOkListener {
                                    try {
                                        replacementRules = GsonUtils
                                        .fromJson(inDialog.input
                                            .toString()
                                            .trim(),
                                            Array<TextReplacement>::class.java)
                                        .toMutableList()
                                    
                                        inDialog.dismiss()
                                        close()
                                    } catch (e: Throwable) {
                                        Utils.showToast("Error: ${e.message}")
                                        inDialog.dismiss()
                                    }
                                }
                inDialog.show(parentFragmentManager, "ImportRules")
            }

            exportBtn.setPadding(p, p, p, p)
            importBtn.setPadding(p, p, p, p)

            exportBtn.setImageDrawable(ContextCompat.getDrawable(ctx, R.e.ic_file_download_white_24dp))
            importBtn.setImageDrawable(ContextCompat.getDrawable(ctx, R.e.ic_file_upload_24dp))

            addHeaderButton(exportBtn)
            addHeaderButton(importBtn)
        }
    }

    private fun updateRules(recycler: RecyclerView) {
        replacementRules.clear()
        var i = 0;
        while (true) {
            val holder = recycler.findViewHolderForLayoutPosition(i) as ReplacerHolder?
            if (holder == null) break
            val replacement = holder.card.createReplacement()
            if (replacement != null) replacementRules.add(replacement)
            i++
        }
    }

}