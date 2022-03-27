package cloudburst.plugins.editwebhooks.ui

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.views.Divider
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.fragments.InputDialog
import com.aliucord.wrappers.ChannelWrapper
import cloudburst.plugins.editwebhooks.ui.WebhookRecyclerAdapter
import com.discord.api.channel.Channel
import com.aliucord.utils.GsonUtils
import cloudburst.plugins.editwebhooks.utils.Webhook
import cloudburst.plugins.editwebhooks.utils.WebhookRequest
import com.aliucord.Http
import com.aliucord.views.Button

class WebhookList(val channelRaw: Channel) : SettingsPage() {

    private val channel = ChannelWrapper(channelRaw)

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle(channel.name)
        
        fetchList()
    }

    public fun fetchList() {
        val context = requireContext()
        Utils.threadPool.execute {
            val list = Http.Request.newDiscordRequest("/channels/%d/webhooks".format(channel.id))
                .setHeader("Referer", "https://discord.com/channels/%d/%d".format(channel.guildId, channel.id))
                .execute()
                .json(Array<Webhook>::class.java)


            Utils.mainThread.post {
                clear()
                val shape = ShapeDrawable(RectShape())
                    .apply {
                        setTint(Color.TRANSPARENT)
                        intrinsicHeight = DimenUtils.defaultPadding
                    }

                val decoration = DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(shape)
                }

                val recyclerView = RecyclerView(context)
                    .apply {
                        adapter = WebhookRecyclerAdapter(list.toList(), this@WebhookList)
                        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                        addItemDecoration(decoration)
                    }

                addView(recyclerView)

                val addButton = Button(context).apply { 
                    text = "Create webhook"
                    setOnClickListener {
                        val inDialog = InputDialog()
                                .setTitle("Create webhook")
                                .setDescription("Enter name of the webhook")
                                .setPlaceholderText("Webhook Name")
                                inDialog.setOnOkListener {
                                    Utils.threadPool.execute {
                                        createWebhook(inDialog.input.toString().trim())
                                    }
                                    inDialog.dismiss()
                                }
                        inDialog.show(parentFragmentManager, "CreateWebhook")
                    }
                }

                addView(Divider(context))
                addView(addButton)
            }
        }
    }

    private fun createWebhook(name: String) {
        Http.Request.newDiscordRequest("/channels/%s/webhooks".format(channel.id), "POST")
            .executeWithJson(WebhookRequest(name, null))
        Utils.showToast("Webhook created")
        fetchList()
    }
}