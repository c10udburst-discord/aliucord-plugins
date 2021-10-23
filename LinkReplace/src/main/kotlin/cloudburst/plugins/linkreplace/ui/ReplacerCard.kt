package cloudburst.plugins.linkreplace.ui

import android.content.Context
import cloudburst.plugins.linkreplace.utils.LinkReplacement
import com.google.android.material.card.MaterialCardView
import com.aliucord.views.TextInput
import com.aliucord.utils.DimenUtils
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

class ReplacerCard(ctx: Context) : MaterialCardView(ctx) {
    val regex: TextInput
    val toDomain: TextInput

    init {
        radius = DimenUtils.defaultCardRadius.toFloat()
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundTertiary))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val p = DimenUtils.defaultPadding

        val linearLayout = com.aliucord.widgets.LinearLayout(ctx).apply {
            regex = TextInput(ctx).apply { 
                hint = "From Regex"
            }
            toDomain = TextInput(ctx).apply { 
                hint = "To Domain"
            }
    
            addView(regex)
            addView(toDomain)
        }
        addView(linearLayout)
    }
}