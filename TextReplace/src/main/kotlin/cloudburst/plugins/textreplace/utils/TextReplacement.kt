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
            TextReplacement("((www|m)\\.)?(youtube\\.com|youtu.be)", "invidious-us.kavin.rocks", true, true, true, true, true),
            TextReplacement("((www|m)\\.)?twitter\\.com", "nitter.net", true, true, true, true, true),
            TextReplacement("bluesmods.com", "aliucord.tk", false, true, false, true, false),
        )
        public fun emptyRule(): TextReplacement {
            return TextReplacement("", "", false, true, true, true, true)
        }
    }
}