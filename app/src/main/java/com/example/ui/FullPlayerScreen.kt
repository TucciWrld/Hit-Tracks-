package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Track
import kotlinx.coroutines.delay

@Composable
fun FullPlayerScreen(
    track: Track,
    viewModel: MusicViewModel,
    primaryColor: Color,
    onDismiss: () -> Unit
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val timeString by viewModel.currentTimeString.collectAsStateWithLifecycle()
    val streamingQuality by viewModel.streamingQuality.collectAsStateWithLifecycle()

    // Infinite rotation for rotating Vinyl
    val infiniteTransition = rememberInfiniteTransition(label = "Vinyl Rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle"
    )

    // Visualizer simulation data generator ticker
    var visualizerFrequencies by remember { mutableStateOf(List(16) { 0.1f }) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                visualizerFrequencies = List(16) { 0.15f + kotlin.random.Random.nextFloat() * 0.8f }
                delay(80)
            }
        } else {
            // Settle frequencies
            while (visualizerFrequencies.any { it > 0.05f }) {
                visualizerFrequencies = visualizerFrequencies.map { (it * 0.7f).coerceAtLeast(0.02f) }
                delay(80)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.25f),
                        Color(0xFF0F0F0F),
                        Color(0xFF0F0F0F)
                    )
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Player Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_player")
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Minimize",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PLAYING FROM HIT TRACKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "NFR Troupe Endorsed",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = { viewModel.toggleLikeTrack(track.id) },
                modifier = Modifier.testTag("player_like_btn")
            ) {
                Icon(
                    imageVector = if (track.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like Track",
                    tint = if (track.isLiked) primaryColor else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Rotating Vinyl Disk Section
        Box(
            modifier = Modifier
                .padding(vertical = 24.dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .border(6.dp, Color(0xFF1E1E2A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = track.title,
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .clip(CircleShape)
                    .rotate(if (isPlaying) rotationAngle else 0f),
                contentScale = ContentScale.Crop
            )

            // Vinyl center dot
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(Color.Black, CircleShape)
                    .border(2.dp, primaryColor, CircleShape)
            )
        }

        // Track Information & Social Share Sheet
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                fontSize = 15.sp,
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Audio Frequency Visualizer Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = 8.dp.toPx()
                val gap = 6.dp.toPx()
                val totalBars = visualizerFrequencies.size
                val startX = (size.width - (totalBars * barWidth + (totalBars - 1) * gap)) / 2f

                for (i in 0 until totalBars) {
                    val frequencyValue = visualizerFrequencies[i]
                    val currentBarHeight = size.height * frequencyValue
                    val x = startX + i * (barWidth + gap)
                    val y = (size.height - currentBarHeight) / 2f

                    drawRoundRect(
                        color = primaryColor,
                        topLeft = androidx.compose.ui.geometry.Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, currentBarHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // Streaming Quality Selector Panel with Security/Copyright locks
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1C1B1F))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val qualities = listOf("Normal", "Ultra HD (320kbps)", "Hi-Fi Lossless")
                qualities.forEach { q ->
                    val isSelected = streamingQuality == q
                    Text(
                        text = q,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color(0xFF8A8A9A),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) primaryColor else Color.Transparent)
                            .clickable { viewModel.setStreamingQuality(q) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Copyright Protection Notice
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copyright Shield: Hi-Fi Lossless streaming is locked online and cannot be downloaded offline.",
                    fontSize = 10.sp,
                    color = Color(0xFF6A6A7A),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Progress Slider / Timers Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Slider(
                value = progress,
                onValueChange = { viewModel.seekTo(it) },
                colors = SliderDefaults.colors(
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = Color(0xFF2B2930),
                    thumbColor = primaryColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_seek_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = timeString, fontSize = 11.sp, color = Color(0xFF8A8A9A))
                Text(text = track.duration, fontSize = 11.sp, color = Color(0xFF8A8A9A))
            }
        }

        // Player controls (Prev, Play/Pause, Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.playPreviousTrack() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("prev_track_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(72.dp)
                    .background(primaryColor, CircleShape)
                    .testTag("play_pause_btn")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(44.dp)
                )
            }

            IconButton(
                onClick = { viewModel.playNextTrack() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("next_track_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next Track",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Social Media Share sheet Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Share song effortlessly to social profiles",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8A8A9A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val socials = listOf(
                    Pair("Twitter", "https://img.icons8.com/color/48/000000/twitter.png"),
                    Pair("Instagram", "https://img.icons8.com/color/48/000000/instagram-new.png"),
                    Pair("Snapchat", "https://img.icons8.com/color/48/000000/snapchat.png"),
                    Pair("Facebook", "https://img.icons8.com/color/48/000000/facebook-new.png")
                )

                socials.forEach { (name, _) ->
                    Button(
                        onClick = { viewModel.shareTrackToSocial(track, name) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E2A)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("share_social_$name"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }
            }
        }
    }
}
