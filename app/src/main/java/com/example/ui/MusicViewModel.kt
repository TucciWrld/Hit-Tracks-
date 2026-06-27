package com.example.ui

import android.app.Application
import android.media.MediaPlayer
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiMoodRecommender
import com.example.api.RecommendedTrack
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val musicDao = MusicDatabase.getDatabase(application).musicDao()
    private val repository = MusicRepository(musicDao, application)

    // Flows from DB
    val allTracks: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedTracks: StateFlow<List<Track>> = repository.downloadedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedTracks: StateFlow<List<Track>> = repository.likedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArtists: StateFlow<List<Artist>> = repository.allArtists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferences
        .map { it ?: UserPreferences() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    // Player State
    private var mediaPlayer: MediaPlayer? = null
    private val _currentPlayingTrack = MutableStateFlow<Track?>(null)
    val currentPlayingTrack: StateFlow<Track?> = _currentPlayingTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _currentTimeString = MutableStateFlow("00:00")
    val currentTimeString: StateFlow<String> = _currentTimeString.asStateFlow()

    // Streaming settings
    private val _streamingQuality = MutableStateFlow("Ultra HD (320kbps)")
    val streamingQuality: StateFlow<String> = _streamingQuality.asStateFlow()

    // Gemini Recommendation State
    private val _moodQuery = MutableStateFlow("")
    val moodQuery: StateFlow<String> = _moodQuery.asStateFlow()

    private val _isRecommending = MutableStateFlow(false)
    val isRecommending: StateFlow<Boolean> = _isRecommending.asStateFlow()

    private val _recommendedTracks = MutableStateFlow<List<RecommendedTrack>>(emptyList())
    val recommendedTracks: StateFlow<List<RecommendedTrack>> = _recommendedTracks.asStateFlow()

    // GDrive input
    private val _gdriveFolderId = MutableStateFlow("")
    val gdriveFolderId: StateFlow<String> = _gdriveFolderId.asStateFlow()

    private val _isSyncingGDrive = MutableStateFlow(false)
    val isSyncingGDrive: StateFlow<Boolean> = _isSyncingGDrive.asStateFlow()

    // Active screen navigation helper
    private val _activeScreen = MutableStateFlow("home")
    val activeScreen: StateFlow<String> = _activeScreen.asStateFlow()

    private val _selectedPlaylistId = MutableStateFlow<String?>(null)
    val selectedPlaylistId: StateFlow<String?> = _selectedPlaylistId.asStateFlow()

    private val _offlineMode = MutableStateFlow(false)
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()

    private var progressJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
        }
    }

    fun navigateTo(screen: String, playlistId: String? = null) {
        _activeScreen.value = screen
        _selectedPlaylistId.value = playlistId
    }

    fun setOfflineMode(enabled: Boolean) {
        _offlineMode.value = enabled
        if (enabled && _currentPlayingTrack.value?.isDownloaded != true) {
            // Stop playing if current track isn't downloaded
            pauseTrack()
            _currentPlayingTrack.value = null
            Toast.makeText(getApplication(), "Offline Mode Active: Online-only streaming disabled", Toast.LENGTH_SHORT).show()
        }
    }

    // Player Actions
    fun playTrack(track: Track) {
        if (_offlineMode.value && !track.isDownloaded) {
            Toast.makeText(getApplication(), "This track is not available offline. Please turn off Offline Mode to stream.", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(track.audioUrl)
                    prepareAsync()
                    setOnPreparedListener { mp ->
                        mp.start()
                        _isPlaying.value = true
                        _currentPlayingTrack.value = track
                        startProgressTracker()
                    }
                    setOnCompletionListener {
                        playNextTrack()
                    }
                    setOnErrorListener { _, what, extra ->
                        Toast.makeText(getApplication(), "Playback Error. High-quality streaming restricted or offline.", Toast.LENGTH_SHORT).show()
                        _isPlaying.value = false
                        false
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Failed to stream: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            progressJob?.cancel()
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracker()
        }
    }

    fun pauseTrack() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                progressJob?.cancel()
            }
        }
    }

    fun seekTo(position: Float) {
        val player = mediaPlayer ?: return
        val duration = player.duration
        val targetMs = (position * duration).toInt()
        player.seekTo(targetMs)
        _playbackProgress.value = position
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch(Dispatchers.Main) {
            while (_isPlaying.value) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val current = player.currentPosition.toFloat()
                        val duration = player.duration.toFloat()
                        if (duration > 0) {
                            _playbackProgress.value = current / duration
                            _currentTimeString.value = formatMs(player.currentPosition)
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun formatMs(ms: Int): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    fun playNextTrack() {
        val tracksList = if (_offlineMode.value) downloadedTracks.value else allTracks.value
        if (tracksList.isEmpty()) return

        val currentIndex = tracksList.indexOfFirst { it.id == _currentPlayingTrack.value?.id }
        val nextIndex = if (currentIndex == -1 || currentIndex == tracksList.lastIndex) 0 else currentIndex + 1
        playTrack(tracksList[nextIndex])
    }

    fun playPreviousTrack() {
        val tracksList = if (_offlineMode.value) downloadedTracks.value else allTracks.value
        if (tracksList.isEmpty()) return

        val currentIndex = tracksList.indexOfFirst { it.id == _currentPlayingTrack.value?.id }
        val prevIndex = if (currentIndex == -1 || currentIndex == 0) tracksList.lastIndex else currentIndex - 1
        playTrack(tracksList[prevIndex])
    }

    // Custom Theme Customization
    fun updateThemeSettings(isDarkMode: Boolean, primaryColorHex: String, fontName: String) {
        viewModelScope.launch {
            repository.savePreferences(isDarkMode, primaryColorHex, fontName)
        }
    }

    // Likes & Followers
    fun toggleLikeTrack(trackId: String) {
        viewModelScope.launch {
            repository.toggleLike(trackId)
        }
    }

    fun toggleDownloadTrack(trackId: String) {
        viewModelScope.launch {
            repository.toggleDownload(trackId)
            Toast.makeText(getApplication(), "Download toggled successfully. Available in Offline Vault.", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFollowArtist(artistName: String) {
        viewModelScope.launch {
            repository.toggleFollowArtist(artistName)
        }
    }

    // Playlist Collaboration
    fun createPlaylist(name: String, description: String, isCollaborative: Boolean) {
        viewModelScope.launch {
            val playlist = repository.createPlaylist(name, description, isCollaborative)
            Toast.makeText(getApplication(), "Collaborative Playlist '${playlist.name}' Created!", Toast.LENGTH_SHORT).show()
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
            Toast.makeText(getApplication(), "Track added to collaborative playlist!", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
            Toast.makeText(getApplication(), "Track removed from playlist", Toast.LENGTH_SHORT).show()
        }
    }

    fun getTracksForPlaylist(playlistId: String): Flow<List<Track>> {
        return repository.getTracksForPlaylist(playlistId)
    }

    // Gemini AI Mood Recommendations
    fun setMoodQuery(query: String) {
        _moodQuery.value = query
    }

    fun getMoodRecommendations() {
        val query = _moodQuery.value
        if (query.isBlank()) return
        _isRecommending.value = true
        viewModelScope.launch {
            val list = GeminiMoodRecommender.getMoodRecommendations(query)
            _recommendedTracks.value = list
            _isRecommending.value = false
        }
    }

    // Social Media Share Simulation
    fun shareTrackToSocial(track: Track, platform: String) {
        val shareMessage = "Streaming '${track.title}' by ${track.artist} on Hit Tracks, endorsed by NFR Troupe Label! #HitTracks #NFRTroupe #SpanningSystems"
        Toast.makeText(getApplication(), "Sharing directly to $platform: \"$shareMessage\"", Toast.LENGTH_LONG).show()
    }

    // Private Direct Messages
    fun sendDirectMessage(recipient: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(recipient, message)
        }
    }

    // GDrive Sync Input
    fun setGDriveFolderId(id: String) {
        _gdriveFolderId.value = id
    }

    fun syncGoogleDriveFolder() {
        val folderId = _gdriveFolderId.value
        _isSyncingGDrive.value = true
        viewModelScope.launch {
            delay(1500) // Simulate network delay
            repository.fetchGoogleDriveTracks(folderId)
            _isSyncingGDrive.value = false
            Toast.makeText(getApplication(), "Synchronized 3 tracks from spanoperatingsystem@gmail.com folder!", Toast.LENGTH_LONG).show()
        }
    }

    fun setStreamingQuality(quality: String) {
        _streamingQuality.value = quality
        Toast.makeText(getApplication(), "Quality switched to $quality", Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        progressJob?.cancel()
    }
}
