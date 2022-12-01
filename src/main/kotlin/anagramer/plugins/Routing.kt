package anagramer.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.locations.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    install(Locations) {}

    val dict = getDictionaryList()

    routing {
        get<RandomWord> { word ->
            println(word)
            val w = dict
                .filter { it.length == word.size }
                .random()
            val words = dict
                .filter { it.length >= word.minimumSize && compare(w, it) }
                .distinctBy { it.lowercase() }
            call.respond(Word(w, words))
        }
    }
}

private fun compare(word: String, anagram: String): Boolean {
    val c = word.groupBy { it.lowercaseChar() }.mapValues { it.value.size }
    val a = anagram.groupBy { it.lowercaseChar() }.mapValues { it.value.size }

    for (i in a) {
        c[i.key]?.let { if (it < i.value) return false } ?: return false
    }

    return true
}

@Serializable
@Location("/randomWord/{size}")
data class RandomWord(val size: Int, val minimumSize: Int = 3)

@Serializable
data class Word(val word: String, val anagrams: List<String>)

fun getDictionaryList(): List<String> {
    return object {}.javaClass.classLoader.getResource("words.txt")
        ?.readText()
        ?.split("\n")
        ?.filterNot { it.contains("-") }
        .orEmpty()
}
