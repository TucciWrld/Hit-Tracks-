package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Artist
import com.example.data.Playlist
import com.example.data.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MusicViewModel) {
    val currentTrack by viewModel.currentPlayingTrack.collectAsStateWithLifecycle()
    val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val offlineMode by viewModel.offlineMode.collectAsStateWithLifecycle()

    // Parse custom primary color
    val primaryColor = remember(preferences.primaryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(preferences.primaryColorHex))
        } catch (e: Exception) {
            Color(0xFFD0BCFF) // fallback Sophisticated Dark Lavender
        }
    }

    // Parse custom font
    val customFontFamily = remember(preferences.fontName) {
        when (preferences.fontName) {
            "Display" -> FontFamily.SansSerif
            "Sans" -> FontFamily.Default
            "Serif" -> FontFamily.Serif
            "Mono" -> FontFamily.Monospace
            else -> FontFamily.Default
        }
    }

    // Full Screen Player state
    var isPlayerExpanded by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = primaryColor,
            background = Color(0xFF0F0F0F),
            surface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFF2B2930),
            onBackground = Color(0xFFE6E1E5),
            onSurface = Color(0xFFE6E1E5),
            onSurfaceVariant = Color(0xFFCAC4D0)
        ),
        typography = MaterialTheme.typography.copy(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = customFontFamily),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = customFontFamily),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = customFontFamily)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        // Expandable bottom player bar
                        if (currentTrack != null) {
                            MiniPlayerBar(
                                track = currentTrack!!,
                                isPlaying = isPlaying,
                                primaryColor = primaryColor,
                                onPlayPauseToggle = { viewModel.togglePlayPause() },
                                onClick = { isPlayerExpanded = true }
                            )
                        }

                        // Bottom Navigation tabs
                        BottomNavBar(
                            activeScreen = activeScreen,
                            onTabSelected = { screen ->
                                viewModel.navigateTo(screen)
                            },
                            primaryColor = primaryColor
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (activeScreen) {
                        "home" -> HomeScreen(viewModel, primaryColor, customFontFamily)
                        "ai_mood" -> MoodScreen(viewModel, primaryColor)
                        "collabs" -> CollabsScreen(viewModel, primaryColor)
                        "offline" -> OfflineScreen(viewModel, primaryColor)
                        "chat" -> ChatScreen(viewModel, primaryColor)
                    }
                }
            }

            // Expanded Full Screen Player Sheet
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                currentTrack?.let { track ->
                    FullPlayerScreen(
                        track = track,
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        onDismiss = { isPlayerExpanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    activeScreen: String,
    onTabSelected: (String) -> Unit,
    primaryColor: Color
) {
    NavigationBar(
        containerColor = Color(0xFF1C1B1F),
        tonalElevation = 8.dp
    ) {
        val tabs = listOf(
            Triple("home", "Home", Icons.Filled.Home),
            Triple("ai_mood", "AI Mood", Icons.Filled.AutoAwesome),
            Triple("collabs", "Collabs", Icons.Filled.Group),
            Triple("offline", "Offline", Icons.Filled.DownloadDone),
            Triple("chat", "Chat", Icons.Filled.Chat)
        )

        tabs.forEach { (route, label, icon) ->
            val isSelected = activeScreen == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryColor,
                    selectedTextColor = primaryColor,
                    indicatorColor = primaryColor.copy(alpha = 0.15f),
                    unselectedIconColor = Color(0xFF938F99),
                    unselectedTextColor = Color(0xFF938F99)
                ),
                modifier = Modifier.testTag("nav_tab_$route")
            )
        }
    }
}

@Composable
fun MiniPlayerBar(
    track: Track,
    isPlaying: Boolean,
    primaryColor: Color,
    onPlayPauseToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("mini_player_bar"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = "${track.title} Cover",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    color = Color(0xFFCAC4D0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onPlayPauseToggle,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("mini_player_play_pause")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    primaryColor: Color,
    fontFamily: FontFamily
) {
    val tracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val artists by viewModel.allArtists.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val gdriveFolderId by viewModel.gdriveFolderId.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncingGDrive.collectAsStateWithLifecycle()
    val offlineMode by viewModel.offlineMode.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Banner / NFR Endorsement
        item {
            NfrEndorsementHeader(primaryColor)
        }

        // Offline Mode Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.WifiOff,
                                contentDescription = "Offline Mode",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Offline Mode Vault",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Disable internet and restrict streaming to downloaded tracks only.",
                            fontSize = 12.sp,
                            color = Color(0xFF8A8A9A),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Switch(
                        checked = offlineMode,
                        onCheckedChange = { viewModel.setOfflineMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = primaryColor,
                            checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("offline_mode_switch")
                    )
                }
            }
        }

        // Google Drive Integration Section
        item {
            GoogleDriveSyncPanel(
                folderId = gdriveFolderId,
                isSyncing = isSyncing,
                primaryColor = primaryColor,
                onFolderIdChanged = { viewModel.setGDriveFolderId(it) },
                onSyncClick = { viewModel.syncGoogleDriveFolder() }
            )
        }

        // Customizable Style Palette Theme
        item {
            ThemeCustomizerPanel(
                preferences = preferences,
                primaryColor = primaryColor,
                onPreferenceChange = { isDark, color, font ->
                    viewModel.updateThemeSettings(isDark, color, font)
                }
            )
        }

        // Following Artist Profiles Section
        item {
            ArtistFollowSection(artists = artists, onFollowToggle = { name ->
                viewModel.toggleFollowArtist(name)
            }, primaryColor = primaryColor)
        }

        // Master Catalog List
        item {
            Text(
                text = "Featured Hit Tracks",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(tracks) { track ->
            TrackRowItem(
                track = track,
                primaryColor = primaryColor,
                onPlayClick = { viewModel.playTrack(track) },
                onLikeClick = { viewModel.toggleLikeTrack(track.id) },
                onDownloadClick = { viewModel.toggleDownloadTrack(track.id) }
            )
        }
    }
}

@Composable
fun NfrEndorsementHeader(primaryColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE50914).copy(alpha = 0.85f), // Crimson red for NFR label vibe
                        Color(0xFF0F0F0F),
                        primaryColor.copy(alpha = 0.4f)
                    )
                )
            )
    ) {
        // Overlapping layers for depth (Brutalism + premium asymmetry)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
                .size(150.dp)
                .rotate(15f)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Verified Label Endorsement",
                    tint = Color(0xFFE50914),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NFR TROUPE OFFICIAL PARTNER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Column {
                Text(
                    text = "Hit Tracks",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Stream raw urban rhythms and high-fidelity soundtracks. Powered by collaborative synergy.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun GoogleDriveSyncPanel(
    folderId: String,
    isSyncing: Boolean,
    primaryColor: Color,
    onFolderIdChanged: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CloudDownload,
                    contentDescription = "Google Drive Sync",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Sync Public Google Drive Folder",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Fetch direct files from spanoperatingsystem@gmail.com's public folder ID.",
                        fontSize = 11.sp,
                        color = Color(0xFF8A8A9A)
                    )
                }
            }

            OutlinedTextField(
                value = folderId,
                onValueChange = onFolderIdChanged,
                placeholder = { Text("Paste GDrive Folder ID here...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gdrive_folder_input"),
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 13.sp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color(0xFF323244)
                )
            )

            Button(
                onClick = onSyncClick,
                enabled = !isSyncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("gdrive_sync_button"),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect & Download Tracks Metadata", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCustomizerPanel(
    preferences: com.example.data.UserPreferences,
    primaryColor: Color,
    onPreferenceChange: (Boolean, String, String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = "Theme Palette",
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aesthetic Theme Customizer",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Color Selector Row
            Column {
                Text("Accent Highlight Color", fontSize = 12.sp, color = Color(0xFF8A8A9A))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val colors = listOf(
                        Pair("Neon Green", "#3DDC84"),
                        Pair("NFR Crimson", "#E50914"),
                        Pair("Cyber Blue", "#00D2FF"),
                        Pair("Tokyo Gold", "#FFD700"),
                        Pair("Neon Pink", "#FF007F")
                    )

                    colors.forEach { (name, hex) ->
                        val isSelected = preferences.primaryColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onPreferenceChange(preferences.isDarkMode, hex, preferences.fontName)
                                }
                                .testTag("theme_color_$name")
                        )
                    }
                }
            }

            // Font Selector Row
            Column {
                Text("Custom Typography Style", fontSize = 12.sp, color = Color(0xFF8A8A9A))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val fonts = listOf("Display", "Sans", "Serif", "Mono")
                    fonts.forEach { font ->
                        val isSelected = preferences.fontName == font
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) primaryColor else Color(0xFF2B2930)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onPreferenceChange(preferences.isDarkMode, preferences.primaryColorHex, font)
                                }
                                .testTag("theme_font_$font")
                        ) {
                            Text(
                                text = font,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistFollowSection(
    artists: List<Artist>,
    onFollowToggle: (String) -> Unit,
    primaryColor: Color
) {
    Column {
        Text(
            text = "Follow NFR Artists & Builders",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(artists) { artist ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(140.dp)
                        .testTag("artist_card_${artist.name}")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = artist.coverUrl,
                            contentDescription = artist.name,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = artist.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${formatFollowers(artist.followersCount)} fans",
                            fontSize = 11.sp,
                            color = Color(0xFF8A8A9A)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onFollowToggle(artist.name) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (artist.isFollowed) Color(0xFF2B2930) else primaryColor
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("follow_btn_${artist.name}"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (artist.isFollowed) "Following" else "Follow",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (artist.isFollowed) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatFollowers(count: Int): String {
    return if (count >= 1000) {
        "${count / 1000}k"
    } else {
        count.toString()
    }
}

@Composable
fun TrackRowItem(
    track: Track,
    primaryColor: Color,
    onPlayClick: () -> Unit,
    onLikeClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayClick() }
            .testTag("track_row_${track.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        fontSize = 12.sp,
                        color = Color(0xFF8A8A9A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = Color(0xFF4A4A5A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = track.duration,
                        fontSize = 11.sp,
                        color = Color(0xFF6A6A7A)
                    )
                }
            }

            // Actions row (Like, Download)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.testTag("like_track_${track.id}")
                ) {
                    Icon(
                        imageVector = if (track.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like Track",
                        tint = if (track.isLiked) primaryColor else Color(0xFF8A8A9A),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.testTag("download_track_${track.id}")
                ) {
                    Icon(
                        imageVector = if (track.isDownloaded) Icons.Filled.DownloadDone else Icons.Filled.Download,
                        contentDescription = "Download offline",
                        tint = if (track.isDownloaded) primaryColor else Color(0xFF8A8A9A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
