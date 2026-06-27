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
