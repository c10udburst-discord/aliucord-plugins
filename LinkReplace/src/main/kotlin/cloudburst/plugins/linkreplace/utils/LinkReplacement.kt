package cloudburst.plugins.linkreplace.utils

data class LinkReplacement(var fromText: String, var toDomain: String) {
    public val fromRegex
        get() = Regex(fromText)

    companion object {
        val DEFAULT_LIST = arrayOf(
            LinkReplacement("((www|m)\\.)?(youtube\\.com|youtu.be)", "invidious-us.kavin.rocks"),
            LinkReplacement("((www|m)\\.)?twitter\\.com", "nitter.net"),
            LinkReplacement("bluesmods\\.com", "aliucord.tk")
        )
    }
}