package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Playlist
import com.example.data.Track

@Composable
fun CollabsScreen(viewModel: MusicViewModel, primaryColor: Color) {
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

    if (selectedPlaylist == null) {
        // List Playlists Screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NFR Collaborative",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .background(primaryColor, RoundedCornerShape(20.dp))
                            .size(40.dp)
                            .testTag("create_playlist_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create Playlist",
                            tint = Color.Black
                        )
                    }
                }
                Text(
                    text = "Co-curated with labels, creators, and fans directly. Connect with others and drop tracks.",
                    fontSize = 13.sp,
                    color = Color(0xFF8A8A9A),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(playlists) { playlist ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlaylist = playlist }
                        .testTag("collab_playlist_card_${playlist.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = playlist.coverUrl,
                            contentDescription = playlist.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = playlist.description,
                                fontSize = 12.sp,
                                color = Color(0xFFB0B0C0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Group,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = playlist.collaborators,
                                    fontSize = 11.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF6A6A7A)
                        )
                    }
                }
            }
        }
    } else {
        // Detailed Playlist Screen
        val activePlaylist = selectedPlaylist!!
        val playlistTracks by viewModel.getTracksForPlaylist(activePlaylist.id).collectAsStateWithLifecycle(emptyList())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedPlaylist = null },
                        modifier = Modifier.testTag("back_to_playlists")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Collaborative Playlist",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Hero Detail
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = activePlaylist.coverUrl,
                        contentDescription = activePlaylist.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = activePlaylist.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = activePlaylist.description,
                            fontSize = 13.sp,
                            color = Color(0xFF8A8A9A),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Created by: ${activePlaylist.creatorName}",
                            fontSize = 11.sp,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            // Collaborators chips
            item {
                Column {
                    Text(
                        text = "Active Co-Curators",
                        fontSize = 12.sp,
                        color = Color(0xFF6A6A7A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activePlaylist.collaborators.split(",").forEach { name ->
                            Text(
                                text = name.trim(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2B2930))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Add Song Row
            item {
                var expandAddSong by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandAddSong = !expandAddSong },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PlaylistAdd, contentDescription = null, tint = primaryColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Collaborate: Add track from Catalog", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(
                                imageVector = if (expandAddSong) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        AnimatedVisibility(visible = expandAddSong) {
                            Column(
                                modifier = Modifier.padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allTracks.forEach { track ->
                                    val isAlreadyIn = playlistTracks.any { it.id == track.id }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (!isAlreadyIn) viewModel.addTrackToPlaylist(activePlaylist.id, track.id)
                                            }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(track.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(track.artist, fontSize = 11.sp, color = Color(0xFF8A8A9A))
                                        }
                                        Icon(
                                            imageVector = if (isAlreadyIn) Icons.Filled.CheckCircle else Icons.Filled.AddCircleOutline,
                                            contentDescription = null,
                                            tint = if (isAlreadyIn) primaryColor else Color(0xFF8A8A9A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Track Header
            item {
                Text(
                    text = "Playlist Tracks (${playlistTracks.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Track Items inside collaborative playlist
            if (playlistTracks.isEmpty()) {
                item {
                    Text(
                        text = "No tracks in this playlist yet. Click Collaborate above to add some bangers!",
                        fontSize = 12.sp,
                        color = Color(0xFF6A6A7A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                }
            } else {
                items(playlistTracks) { track ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F2A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(track.artist, fontSize = 11.sp, color = Color(0xFF8A8A9A))
                            }

                            // Play inside collaborative playlist
                            IconButton(onClick = { viewModel.playTrack(track) }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = primaryColor)
                            }

                            // Remove from collaborative playlist
                            IconButton(
                                onClick = { viewModel.removeTrackFromPlaylist(activePlaylist.id, track.id) },
                                modifier = Modifier.testTag("remove_track_${track.id}")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFE50914))
                            }
                        }
                    }
                }
            }
        }
    }

    // Create playlist Dialog
    if (showCreateDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Collaborative Playlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        placeholder = { Text("Playlist Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_playlist_name_input")
                    )

                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        placeholder = { Text("Playlist Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_playlist_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.createPlaylist(playlistName, playlistDesc, isCollaborative = true)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.testTag("confirm_create_playlist")
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1C1B1F)
        )
    }
}
