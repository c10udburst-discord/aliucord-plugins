package cloudburst.plugins.textreplace.utils

data class TextReplacement(
    val fromInput: String, 
    val replacement: String, 
    val isRegex: Boolean,
    val ignoreCase: Boolean,
    val matchUnsent: Boolean,
    val matchSent: Boolean,
    val matchEmbeds: Boolean) {
        public fun matches(message: String): Boolean {
            if (isRegex) {
                return fromInput.toRegex().containsMatchIn(message)
            } else {
                return message.contains(fromInput)
            }
        }

        public fun replace(message: String): String {
            if (isRegex) {
                return fromInput.toRegex().replace(message, replacement)
            } else {
                return message.replace(fromInput, replacement, ignoreCase)
            }
        }

    companion object {
        val DEFAULT_LIST = arrayOf(
            TextReplacement("media.discordapp.net", "cdn.discordapp.com", false, true, true, false, false),
            TextReplacement("bluesmods.com", "aliucord.tk", false, true, true, false, false),
        )
        public fun emptyRule(): TextReplacement {
            return TextReplacement("", "", false, true, true, true, true)
        }
    }
}
