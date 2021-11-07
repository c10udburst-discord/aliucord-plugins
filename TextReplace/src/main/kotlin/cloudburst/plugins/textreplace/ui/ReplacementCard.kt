package cloudburst.plugins.textreplace.ui

import android.content.Context
import com.google.android.material.card.MaterialCardView
import com.aliucord.views.TextInput
import com.aliucord.utils.DimenUtils
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R
import cloudburst.plugins.textreplace.utils.TextReplacement
import com.discord.views.CheckedSetting
import com.aliucord.Utils

class ReplacerCard(ctx: Context) : MaterialCardView(ctx) {
    val fromInput: TextInput 
    val replacementInput: TextInput 
    val isRegex: CheckedSetting 
    val ignoreCase: CheckedSetting 
    val matchUnsent: CheckedSetting 
    val matchSent: CheckedSetting 
    val matchEmbeds: CheckedSetting 

    init {
        radius = DimenUtils.defaultCardRadius.toFloat()
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundTertiary))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val p = DimenUtils.defaultPadding
        val linearLayout = com.aliucord.widgets.LinearLayout(ctx).apply { 
            setPadding(p, p, p, p)
        }

        fromInput = TextInput(ctx, "From")
        linearLayout.addView(fromInput)

        replacementInput = TextInput(ctx, "To")
        linearLayout.addView(replacementInput)

        isRegex = Utils.createCheckedSetting(
            ctx,
            CheckedSetting.ViewType.SWITCH,
            "Regex",
            "Whether to interpret from as regex"
        )
        linearLayout.addView(isRegex)
        
        ignoreCase = Utils.createCheckedSetting(
            ctx,
            CheckedSetting.ViewType.SWITCH,
            "Case Insensitive",
            "Whether to ignore if upper or lower case"
        )
        linearLayout.addView(ignoreCase)

        matchUnsent = Utils.createCheckedSetting(
            ctx,
            CheckedSetting.ViewType.SWITCH,
            "Match unsent",
            "Whether to match unsent messages"
        )
        linearLayout.addView(matchUnsent)

        matchSent = Utils.createCheckedSetting(
            ctx,
            CheckedSetting.ViewType.SWITCH,
            "Match unsent",
            "Whether to match sent messages"
        )
        linearLayout.addView(matchSent)

        matchEmbeds = Utils.createCheckedSetting(
            ctx,
            CheckedSetting.ViewType.SWITCH,
            "Match embeds",
            "Whether to match embeds"
        )
        linearLayout.addView(matchEmbeds)

        addView(linearLayout)
    }

    public fun apply(replacement: TextReplacement) {
        fromInput.editText.setText(replacement.fromInput)
        replacementInput.editText.setText(replacement.replacement)
        isRegex.isChecked = replacement.isRegex
        ignoreCase.isChecked = replacement.ignoreCase
        matchUnsent.isChecked = replacement.matchUnsent
        matchSent.isChecked = replacement.matchSent
        matchEmbeds.isChecked = replacement.matchEmbeds
    }

    public fun createReplacement(): TextReplacement? {
        if ((fromInput.editText.text.toString() == "") or (replacementInput.editText.text.toString() == "")) return null
        try {
            return TextReplacement(
                fromInput.editText.text.toString(),
                replacementInput.editText.text.toString(),
                isRegex.isChecked,
                ignoreCase.isChecked,
                matchUnsent.isChecked,
                matchSent.isChecked,
                matchEmbeds.isChecked,
            )
        } catch (e: Throwable) {
            
        }
        return null
    }
}