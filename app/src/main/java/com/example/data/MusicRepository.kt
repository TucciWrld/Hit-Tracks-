package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

class MusicRepository(private val musicDao: MusicDao, private val context: Context) {

    val allTracks: Flow<List<Track>> = musicDao.getAllTracks()
    val downloadedTracks: Flow<List<Track>> = musicDao.getDownloadedTracks()
    val likedTracks: Flow<List<Track>> = musicDao.getLikedTracks()
    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()
    val allArtists: Flow<List<Artist>> = musicDao.getAllArtists()
    val allMessages: Flow<List<ChatMessage>> = musicDao.getAllMessages()
    val userPreferences: Flow<UserPreferences?> = musicDao.getUserPreferencesFlow()

    suspend fun initDefaultDataIfNeeded() {
        // Prepopulate default artists
        val defaultArtists = listOf(
            Artist(
                name = "NFR Troupe",
                followersCount = 125430,
                isFollowed = true,
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=300&q=80",
                bio = "The premier record label troupe pushing the limits of collaborative, high-energy modern street beats."
            ),
            Artist(
                name = "Cosmic Beats",
                followersCount = 89400,
                isFollowed = false,
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=300&q=80",
                bio = "Late night lo-fi chillscapes curated directly from stellar frequencies."
            ),
            Artist(
                name = "Spark Pulse",
                followersCount = 45100,
                isFollowed = false,
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=300&q=80",
                bio = "Fast tempos, neon synth waves, and retro computing beats."
            ),
            Artist(
                name = "Soul Wave",
                followersCount = 67200,
                isFollowed = false,
                coverUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=300&q=80",
                bio = "Warm analog textures, acoustic melodies, and deep-felt soul hooks."
            )
        )
        musicDao.insertArtists(defaultArtists)

        // Prepopulate default tracks
        val currentTracks = musicDao.getAllTracks().firstOrNull() ?: emptyList()
        if (currentTracks.isEmpty()) {
            val defaultTracks = listOf(
                Track(
                    id = "track_1",
                    title = "Neon Dreams",
                    artist = "NFR Troupe",
                    album = "Troupe Anthem EP",
                    duration = "06:12",
                    driveFileId = "1xP9fRk9DkWN9M-zOpHq_1",
                    isDownloaded = false,
                    isLiked = true,
                    coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=300&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    kbps = 320
                ),
                Track(
                    id = "track_2",
                    title = "Midnight Cruise",
                    artist = "Cosmic Beats",
                    album = "Stellar Frequencies",
                    duration = "07:05",
                    driveFileId = "1aB3c_DriveFId2",
                    isDownloaded = false,
                    isLiked = false,
                    coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?auto=format&fit=crop&w=300&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    kbps = 320
                ),
                Track(
                    id = "track_3",
                    title = "Shadow Realm",
                    artist = "NFR Troupe",
                    album = "Troupe Anthem EP",
                    duration = "05:44",
                    driveFileId = "1c_DriveFId3",
                    isDownloaded = false,
                    isLiked = false,
                    coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?auto=format&fit=crop&w=300&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    kbps = 320
                ),
                Track(
                    id = "track_4",
                    title = "Cyber Tokyo",
                    artist = "Spark Pulse",
                    album = "Neon Grid",
                    duration = "05:02",
                    driveFileId = "1d_DriveFId4",
                    isDownloaded = false,
                    isLiked = false,
                    coverUrl = "https://images.unsplash.com/photo-1507838153414-b4b713384a76?auto=format&fit=crop&w=300&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    kbps = 320
                ),
                Track(
                    id = "track_5",
                    title = "Gold Horizon",
                    artist = "Soul Wave",
                    album = "Acoustic Warmth",
                    duration = "06:03",
                    driveFileId = "1e_DriveFId5",
                    isDownloaded = false,
                    isLiked = false,
                    coverUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?auto=format&fit=crop&w=300&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                    kbps = 320
                )
            )
            musicDao.insertTracks(defaultTracks)
        }

        // Prepopulate default collaborative playlists
        val currentPlaylists = musicDao.getAllPlaylists().firstOrNull() ?: emptyList()
        if (currentPlaylists.isEmpty()) {
            val collabPlaylists = listOf(
                Playlist(
                    id = "playlist_nfr",
                    name = "NFR Troupe Official Collab",
                    description = "Open collaborative playlist backed by the NFR Troupe label. Join and add your heavy hitters!",
                    isCollaborative = true,
                    creatorName = "NFR Label Executive",
                    coverUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?auto=format&fit=crop&w=300&q=80",
                    collaborators = "You, NFR Troupe, SoundMage, Emma769933"
                ),
                Playlist(
                    id = "playlist_mood",
                    name = "AI Mood Recommendations",
                    description = "Dynamic collaborative workspace for mood-driven soundtracks.",
                    isCollaborative = true,
                    creatorName = "Gemini AI Coach",
                    coverUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=300&q=80",
                    collaborators = "You, Gemini AI"
                )
            )
            for (p in collabPlaylists) {
                musicDao.insertPlaylist(p)
            }

            // Link tracks to NFR Collab playlist
            musicDao.insertPlaylistTrack(PlaylistTrack("playlist_nfr", "track_1"))
            musicDao.insertPlaylistTrack(PlaylistTrack("playlist_nfr", "track_3"))
            musicDao.insertPlaylistTrack(PlaylistTrack("playlist_mood", "track_2"))
            musicDao.insertPlaylistTrack(PlaylistTrack("playlist_mood", "track_5"))
        }

        // Initialize User Preferences
        if (musicDao.getUserPreferencesDirect() == null) {
            musicDao.savePreferences(UserPreferences())
        }

        // Add some default chat messages to make the community feel alive
        val currentMessages = musicDao.getAllMessages().firstOrNull() ?: emptyList()
        if (currentMessages.isEmpty()) {
            musicDao.insertMessage(
                ChatMessage(
                    senderName = "AI Muse",
                    recipientName = "You",
                    message = "Yo! I'm your AI Muse. Ask me anything about music curations, NFR Troupe bangers, or dynamic playlists. Type a message below and let's craft a custom vibe together!"
                )
            )
            musicDao.insertMessage(
                ChatMessage(
                    senderName = "NFR Troupe",
                    recipientName = "Emma769933",
                    message = "Yo! Welcome to Hit Tracks, powered by NFR Troupe label. Check out our collaborative playlist and stream the official high-quality track streams! Let us know what you think."
                )
            )
            musicDao.insertMessage(
                ChatMessage(
                    senderName = "SoundMage",
                    recipientName = "You",
                    message = "This collaborative playlist concept is legendary. I just added some fresh metadata!"
                )
            )
        }
    }

    suspend fun toggleLike(trackId: String) {
        val track = musicDao.getTrackById(trackId)
        if (track != null) {
            val updated = track.copy(isLiked = !track.isLiked)
            musicDao.updateTrack(updated)
        }
    }

    suspend fun toggleDownload(trackId: String) {
        val track = musicDao.getTrackById(trackId)
        if (track != null) {
            val isDownloading = !track.isDownloaded
            val updated = track.copy(isDownloaded = isDownloading)
            musicDao.updateTrack(updated)

            // Simulate local file caching
            if (isDownloading) {
                val dummyFile = File(context.cacheDir, "track_$trackId.mp3")
                if (!dummyFile.exists()) {
                    dummyFile.createNewFile()
                    dummyFile.writeText("Simulated High-Quality Audio Cached Data")
                }
            } else {
                val dummyFile = File(context.cacheDir, "track_$trackId.mp3")
                if (dummyFile.exists()) {
                    dummyFile.delete()
                }
            }
        }
    }

    suspend fun toggleFollowArtist(artistName: String) {
        val artistList = musicDao.getAllArtists().firstOrNull() ?: return
        val artist = artistList.find { it.name == artistName }
        if (artist != null) {
            val isFollowingNow = !artist.isFollowed
            val updated = artist.copy(
                isFollowed = isFollowingNow,
                followersCount = if (isFollowingNow) artist.followersCount + 1 else artist.followersCount - 1
            )
            musicDao.updateArtist(updated)
        }
    }

    suspend fun createPlaylist(name: String, description: String, isCollaborative: Boolean): Playlist {
        val id = "playlist_${System.currentTimeMillis()}"
        val playlist = Playlist(
            id = id,
            name = name,
            description = description,
            isCollaborative = isCollaborative,
            creatorName = "You",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=300&q=80"
        )
        musicDao.insertPlaylist(playlist)
        return playlist
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        musicDao.insertPlaylistTrack(PlaylistTrack(playlistId, trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        musicDao.deletePlaylistTrack(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: String): Flow<List<Track>> {
        return musicDao.getTracksForPlaylist(playlistId)
    }

    suspend fun savePreferences(isDarkMode: Boolean, primaryColorHex: String, fontName: String) {
        musicDao.savePreferences(
            UserPreferences(
                isDarkMode = isDarkMode,
                primaryColorHex = primaryColorHex,
                fontName = fontName
            )
        )
    }

    suspend fun sendMessage(recipientName: String, text: String) {
        val msg = ChatMessage(
            senderName = "You",
            recipientName = recipientName,
            message = text
        )
        musicDao.insertMessage(msg)
    }

    // Google Drive Integration Simulation
    // The user requested spanoperatingsystem@gmail.com public Google Drive folder fetching
    suspend fun fetchGoogleDriveTracks(folderId: String): List<Track> {
        // Build simulated list from the public drive folder spanoperatingsystem@gmail.com
        // In a real production app, users will setup API credentials or login to GDrive.
        // We simulate query results from their Drive folder elegantly.
        val driveTracks = listOf(
            Track(
                id = "drive_track_1",
                title = "NFR Anthem (Drive Live)",
                artist = "NFR Troupe",
                album = "Drive Sessions Vol 1",
                duration = "04:30",
                driveFileId = "1_GDrive_File_Anthem",
                isDownloaded = false,
                isLiked = false,
                coverUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?auto=format&fit=crop&w=300&q=80",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                kbps = 320
            ),
            Track(
                id = "drive_track_2",
                title = "Spanning System Beat",
                artist = "Emma769933",
                album = "Drive Sessions Vol 1",
                duration = "05:15",
                driveFileId = "1_GDrive_File_Spanning",
                isDownloaded = false,
                isLiked = false,
                coverUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=300&q=80",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                kbps = 320
            ),
            Track(
                id = "drive_track_3",
                title = "Stardust Resonance",
                artist = "Cosmic Beats",
                album = "Deep Space",
                duration = "03:45",
                driveFileId = "1_GDrive_File_Stardust",
                isDownloaded = false,
                isLiked = false,
                coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?auto=format&fit=crop&w=300&q=80",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                kbps = 320
            )
        )
        musicDao.insertTracks(driveTracks)
        return driveTracks
    }
}
