package cloudburst.plugins.sendembeds.utils

data class Embed(val author: Author, val title: String, val description: String, val url: String, val image: EmbedImage?, val color: Int)