package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class RecommendedTrack(
    val title: String,
    val artist: String,
    val description: String,
    val duration: String
)

object GeminiMoodRecommender {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun getMoodRecommendations(moodPrompt: String): List<RecommendedTrack> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackRecommendations(moodPrompt)
        }

        val prompt = "Recommend a customized music playlist of 4 songs aligned with the mood: \"$moodPrompt\". Present a friendly reason why each song fits this specific atmosphere. Only suggest songs that would fit under the NFR Troupe Label style (hip hop, trap, electronic retro, deep lo-fi, or energetic synthwave). Return response strictly in JSON array format with fields 'title', 'artist', 'description', and 'duration' for each song. Do not include markdown codeblocks or triple backticks."

        // Build the request body manually or using map for maximum simplicity
        val requestMap = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to prompt)
                    )
                )
            )
        )

        val requestAdapter = moshi.adapter(Map::class.java)
        val jsonRequest = requestAdapter.toJson(requestMap)

        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonRequest.toRequestBody(mediaType)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val httpRequest = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext getLocalFallbackRecommendations(moodPrompt)
                }

                val responseBody = response.body?.string() ?: ""
                val responseAdapter = moshi.adapter(Map::class.java)
                val parsedResponse = responseAdapter.fromJson(responseBody)

                // Navigate response map to extract text
                val candidates = parsedResponse?.get("candidates") as? List<*>
                val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
                val content = firstCandidate?.get("content") as? Map<*, *>
                val parts = content?.get("parts") as? List<*>
                val firstPart = parts?.firstOrNull() as? Map<*, *>
                val textOutput = firstPart?.get("text") as? String

                if (textOutput != null) {
                    // Clean code blocks if returned
                    val cleanJson = textOutput
                        .trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val listType = Types.newParameterizedType(List::class.java, RecommendedTrack::class.java)
                    val adapter = moshi.adapter<List<RecommendedTrack>>(listType)
                    adapter.fromJson(cleanJson) ?: getLocalFallbackRecommendations(moodPrompt)
                } else {
                    getLocalFallbackRecommendations(moodPrompt)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalFallbackRecommendations(moodPrompt)
        }
    }

    private fun getLocalFallbackRecommendations(moodPrompt: String): List<RecommendedTrack> {
        val lower = moodPrompt.lowercase()
        return when {
            lower.contains("chill") || lower.contains("lo-fi") || lower.contains("relax") || lower.contains("study") -> {
                listOf(
                    RecommendedTrack("Midnight Cruise", "Cosmic Beats", "Smooth electric keys perfect for late night relaxation.", "07:05"),
                    RecommendedTrack("Gold Horizon", "Soul Wave", "Warm acoustic guitar chords to soothe the mind.", "06:03"),
                    RecommendedTrack("Quiet Resonance", "Cosmic Beats", "A minimalist ambient background that creates breathing room.", "03:45"),
                    RecommendedTrack("Stardust Drift", "Emma769933", "A slow space-themed beat to unwind after a long session.", "04:12")
                )
            }
            lower.contains("hype") || lower.contains("energetic") || lower.contains("workout") || lower.contains("trap") || lower.contains("rap") -> {
                listOf(
                    RecommendedTrack("Neon Dreams", "NFR Troupe", "High octane 808s and crisp snare hits endorsed by NFR Troupe.", "06:12"),
                    RecommendedTrack("Shadow Realm", "NFR Troupe", "Heavy sub-bass lines and street-inspired lyrics.", "05:44"),
                    RecommendedTrack("Cyber Tokyo", "Spark Pulse", "Adrenaline-fueled synthwave track keeping your heartbeat racing.", "05:02"),
                    RecommendedTrack("NFR Anthem (Drive Live)", "NFR Troupe", "The raw energy of NFR recorded live from GDrive folder.", "04:30")
                )
            }
            else -> {
                listOf(
                    RecommendedTrack("Neon Dreams", "NFR Troupe", "A high-energy banger highlighting NFR's premier production style.", "06:12"),
                    RecommendedTrack("Midnight Cruise", "Cosmic Beats", "A premium lofi track establishing the perfect aesthetic mood.", "07:05"),
                    RecommendedTrack("Cyber Tokyo", "Spark Pulse", "Retro futuristic electronic synthwave for a unique atmosphere.", "05:02"),
                    RecommendedTrack("Gold Horizon", "Soul Wave", "Uplifting and warm textures providing structural comfort.", "06:03")
                )
            }
        }
    }
}

object GeminiChatBot {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun getAiChatResponse(userMessage: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackChatResponse(userMessage)
        }

        val prompt = "You are 'AI Muse', an exclusive, cool AI music assistant, record label co-curator, and expert DJ for the Hit Tracks app. Respond to this message in a friendly, witty, brief and cool style (max 2-3 sentences): \"$userMessage\". Keep your advice connected to hip hop, lo-fi, synthwave, or trap music styles typical of the NFR Troupe Label. Return a pure text response, with absolutely no markdown syntax, no backticks, and no bullet points. Be direct, warm, and highly professional."

        val requestMap = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to prompt)
                    )
                )
            )
        )

        val requestAdapter = moshi.adapter(Map::class.java)
        val jsonRequest = requestAdapter.toJson(requestMap)

        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonRequest.toRequestBody(mediaType)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val httpRequest = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext getLocalFallbackChatResponse(userMessage)
                }

                val responseBody = response.body?.string() ?: ""
                val responseAdapter = moshi.adapter(Map::class.java)
                val parsedResponse = responseAdapter.fromJson(responseBody)

                val candidates = parsedResponse?.get("candidates") as? List<*>
                val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
                val content = firstCandidate?.get("content") as? Map<*, *>
                val parts = content?.get("parts") as? List<*>
                val firstPart = parts?.firstOrNull() as? Map<*, *>
                val textOutput = firstPart?.get("text") as? String

                textOutput?.trim() ?: getLocalFallbackChatResponse(userMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalFallbackChatResponse(userMessage)
        }
    }

    private fun getLocalFallbackChatResponse(userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("recommend") || lower.contains("song") || lower.contains("track") -> {
                "Oh, definitely stream 'Neon Dreams' by NFR Troupe! It features incredible 808 sub-bass, or if you are in a laidback mood, put on 'Midnight Cruise' by Cosmic Beats. Perfect vibe for high-quality audio streaming!"
            }
            lower.contains("hi") || lower.contains("hello") || lower.contains("yo") -> {
                "Yo! I'm AI Muse, your personal music assistant for Hit Tracks. Need any fresh playlist ideas, artist lore, or NFR Troupe recommendations? Ask away!"
            }
            lower.contains("playlist") -> {
                "For playlists, check out 'Road Trip Mix' or use our 'AI Recommendation' tab to generate a custom Late Night Flow based on any prompt!"
            }
            else -> {
                "That's high-level taste right there. I love how you're thinking about music. In the NFR Troupe style, everything is about that crisp production and rich bass depth. Anything else you want to explore?"
            }
        }
    }
}
