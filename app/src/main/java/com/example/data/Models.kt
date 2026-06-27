package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val driveFileId: String,
    val isDownloaded: Boolean = false,
    val isLiked: Boolean = false,
    val coverUrl: String,
    val audioUrl: String,
    val kbps: Int = 320 // high-quality audio streaming (e.g. 320kbps or 128kbps lossless/high)
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val isCollaborative: Boolean = true,
    val creatorName: String,
    val coverUrl: String,
    val collaborators: String = "You" // comma-separated list of collaborator names
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrack(
    val playlistId: String,
    val trackId: String
)

@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey val name: String,
    val followersCount: Int,
    val isFollowed: Boolean = false,
    val coverUrl: String,
    val bio: String
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val recipientName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = true,
    val primaryColorHex: String = "#D0BCFF", // default Sophisticated Dark Lavender
    val fontName: String = "Display" // Display, Sans, Serif, Mono
)
