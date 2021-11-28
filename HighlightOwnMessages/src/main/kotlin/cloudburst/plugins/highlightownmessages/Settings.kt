package cloudburst.plugins.highlightownmessages

import com.aliucord.fragments.SettingsPage
import android.view.View
import android.view.Gravity

import com.aliucord.Utils
import com.aliucord.utils.DimenUtils
import com.discord.views.CheckedSetting
import com.aliucord.widgets.LinearLayout
import com.aliucord.api.SettingsAPI
import android.widget.TextView
import com.lytefast.flexinput.R
import com.aliucord.views.TextInput
import android.text.InputType
import com.discord.utilities.colors.ColorPickerUtils
import android.graphics.Color

class Settings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("HighlightOwnMessages")
        val p = DimenUtils.defaultPadding / 2

        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Right and Left", "Whether to make your messages show on the right.").apply {
            val key = "RightLeft"
            isChecked = settings.getBool(key, true)
            setOnCheckedListener {
                settings.setBool(key, it)
            }
        })

        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Left Align Multiline", "Whether to make multiline text left aligned, but add padding.").apply {
            val key = "Multiline"
            isChecked = settings.getBool(key, false)
            setOnCheckedListener {
                settings.setBool(key, it)
            }
        })

        val padding = TextInput(view.context).apply {
            setHint("Padding")
            editText.setText(settings.getInt("Padding", 256).toString())
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.maxLines = 1
            setPadding(p, p, p, p)
        }
        addView(padding)

        addView(TextView(view.context, null, 0, R.i.UiKit_TextView).apply { 
            text = "Set color to 0 if you wish to disable changing colors. Click the edit icon to open a color picker."
            setPadding(p, p, p, p)
        })

        var selfFg: View? = null
        selfFg = TextInput(view.context, "Self Foreground Color", settings.getInt("SelfFg", 0).toString(), View.OnClickListener {
            if (selfFg != null) colorPicker(selfFg as TextInput)
        }).apply {
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.maxLines = 1
            setThemedEndIcon(R.e.ic_theme_24dp)
            setPadding(p, p, p, p)
        }
        addView(selfFg)

        var selfBg: View? = null
        selfBg = TextInput(view.context, "Self Background Color", settings.getInt("SelfBg", 0).toString(), View.OnClickListener {
            if (selfBg != null) colorPicker(selfBg as TextInput)
        }).apply {
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.maxLines = 1
            setThemedEndIcon(R.e.ic_theme_24dp)
            setPadding(p, p, p, p)
        }
        addView(selfBg)

        setOnBackPressed {
            try {
                settings.setInt("Padding", padding.editText.text.toString().toInt())
                settings.setInt("SelfFg", selfFg.editText.text.toString().toInt())
                settings.setInt("SelfBg", selfBg.editText.text.toString().toInt())
            } catch(e:Throwable) {
                Utils.showToast(e.message.toString())
            }
            return@setOnBackPressed false
        }
    }


    private fun colorPicker(input: TextInput) {
        val builder = ColorPickerUtils.INSTANCE.buildColorPickerDialog(
            context, 
            Utils.getResId("color_picker_title", "string"), 
            Color.BLACK
        )
        builder.arguments?.putBoolean("alpha", true)
        builder.j = object: c.k.a.a.f { // color picker listener i guess
            override fun onColorReset(i: Int) { }

            override fun onColorSelected(i: Int, i2: Int) {
                try {
                    input.editText.setText(i2.toString())
                } catch(e:Throwable) {
                    Utils.showToast(e.message.toString())
                }
            }

            override fun onDialogDismissed(i: Int) { }
        }
        builder.show(parentFragmentManager, "COLOR_PICKER")
    }
}