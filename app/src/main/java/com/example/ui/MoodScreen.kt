package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Track

@Composable
fun MoodScreen(viewModel: MusicViewModel, primaryColor: Color) {
    val moodQuery by viewModel.moodQuery.collectAsStateWithLifecycle()
    val isRecommending by viewModel.isRecommending.collectAsStateWithLifecycle()
    val recommendedTracks by viewModel.recommendedTracks.collectAsStateWithLifecycle()
    val allPlaylists by viewModel.allPlaylists.collectAsStateWithLifecycle()

    val quickChips = listOf(
        "Warm Acoustic Horizon",
        "NFR Heavy Street Trap",
        "Late Night Cyber Chill",
        "Fast Retro Synthwave"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Mood Recommender",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Text(
                text = "Describe your vibe, and Gemini will instantly curate custom tracks tailored to your frequency.",
                fontSize = 13.sp,
                color = Color(0xFF8A8A9A),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        // Mood input text field
        item {
            OutlinedTextField(
                value = moodQuery,
                onValueChange = { viewModel.setMoodQuery(it) },
                label = { Text("What's your current atmosphere?") },
                placeholder = { Text("e.g. driving alone through a neon cyberpunk grid after midnight") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mood_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedBorderColor = Color(0xFF323244)
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.getMoodRecommendations() },
                        enabled = !isRecommending && moodQuery.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Search",
                            tint = if (moodQuery.isNotBlank()) primaryColor else Color(0xFF4A4A5A)
                        )
                    }
                }
            )
        }

        // Quick Suggestion Chips
        item {
            Text("Quick Presets", fontSize = 12.sp, color = Color(0xFF6A6A7A))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickChips) { chip ->
                    val isSelected = moodQuery == chip
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) primaryColor else Color(0xFF1C1B1F)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clickable { viewModel.setMoodQuery(chip) }
                            .testTag("mood_preset_chip_$chip")
                    ) {
                        Text(
                            text = chip,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Action trigger button
        item {
            Button(
                onClick = { viewModel.getMoodRecommendations() },
                enabled = !isRecommending && moodQuery.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("trigger_recommendations_button"),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                if (isRecommending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Curate AI Tracklist", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        // Recommendation Results Title
        if (recommendedTracks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curated For You",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Gemini flash",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            items(recommendedTracks) { track ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Play matching local/simulated track or show quick stream alert
                            viewModel.playTrack(
                                Track(
                                    id = "curated_${track.title.hashCode()}",
                                    title = track.title,
                                    artist = track.artist,
                                    album = "Curated Mood",
                                    duration = track.duration,
                                    driveFileId = "1_GDrive_Curated",
                                    isDownloaded = false,
                                    isLiked = false,
                                    coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=300&q=80",
                                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
                                )
                            )
                        }
                        .testTag("curated_track_${track.title}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artist,
                                    fontSize = 12.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = track.duration,
                                fontSize = 12.sp,
                                color = Color(0xFF6A6A7A)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = track.description,
                                fontSize = 12.sp,
                                color = Color(0xFFB0B0C0),
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Add to Collaborative Playlist quick action
                        if (allPlaylists.isNotEmpty()) {
                            val playlist = allPlaylists.first()
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2B2930))
                                    .clickable {
                                        viewModel.addTrackToPlaylist(
                                            playlist.id,
                                            "track_1" // Adds Neon Dreams as anchor track with curated meta
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlaylistAdd,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Collab: Add to ${playlist.name}",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Empty curation placeholder
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.LibraryMusic,
                            contentDescription = null,
                            tint = Color(0xFF323244),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tracks curated yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A6A7A)
                        )
                        Text(
                            text = "Type your mood above and click curate to witness Gemini API magic.",
                            fontSize = 12.sp,
                            color = Color(0xFF4A4A5A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
