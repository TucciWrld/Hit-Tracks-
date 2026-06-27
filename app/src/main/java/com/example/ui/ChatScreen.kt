package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(viewModel: MusicViewModel, primaryColor: Color) {
    val messages by viewModel.allMessages.collectAsStateWithLifecycle()
    var selectedContact by remember { mutableStateOf("NFR Troupe") }
    var typedMessage by remember { mutableStateOf("") }

    val contacts = listOf(
        Pair("NFR Troupe", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=300&q=80"),
        Pair("SoundMage", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=300&q=80"),
        Pair("Emma769933", "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?auto=format&fit=crop&w=300&q=80")
    )

    // filter messages for active thread
    val threadMessages = remember(messages, selectedContact) {
        messages.filter {
            (it.senderName == "You" && it.recipientName == selectedContact) ||
            (it.senderName == selectedContact && it.recipientName == "You") ||
            (it.senderName == selectedContact && it.recipientName == "Emma769933") // fallback thread mock helper
        }
    }

    val listState = rememberLazyListState()

    // Scroll chat to end when new messages arrive
    LaunchedEffect(threadMessages.size) {
        if (threadMessages.isNotEmpty()) {
            listState.animateScrollToItem(threadMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chat Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Chat,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Fan Messages & Chat",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        // Contact Selector Row
        Column {
            Text(
                text = "Select Contact",
                fontSize = 11.sp,
                color = Color(0xFF6A6A7A),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(contacts) { (name, url) ->
                    val isSelected = selectedContact == name
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) primaryColor.copy(alpha = 0.2f) else Color(0xFF1C1B1F)
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, primaryColor) else null,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .clickable { selectedContact = name }
                            .testTag("contact_tab_$name")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = name,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) primaryColor else Color.White
                            )
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFF2B2930))

        // Message List Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (threadMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Chat, contentDescription = null, tint = Color(0xFF2B2930), modifier = Modifier.size(60.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No messages in this vault", fontSize = 14.sp, color = Color(0xFF6A6A7A))
                            Text("Send a message to start collaboration!", fontSize = 11.sp, color = Color(0xFF4A4A5A))
                        }
                    }
                }
            } else {
                items(threadMessages) { msg ->
                    val isOwn = msg.senderName == "You"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isOwn) {
                                val url = contacts.find { it.first == msg.senderName }?.second ?: ""
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .padding(end = 4.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOwn) primaryColor else Color(0xFF1E1E2A)
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isOwn) 12.dp else 0.dp,
                                    bottomEnd = if (isOwn) 0.dp else 12.dp
                                ),
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .testTag("chat_bubble_${msg.id}")
                            ) {
                                Text(
                                    text = msg.message,
                                    fontSize = 13.sp,
                                    color = if (isOwn) Color.Black else Color.White,
                                    fontWeight = if (isOwn) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Date formatter
                        Text(
                            text = formatTime(msg.timestamp),
                            fontSize = 9.sp,
                            color = Color(0xFF6A6A7A),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Message input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = typedMessage,
                onValueChange = { typedMessage = it },
                placeholder = { Text("Write a message to $selectedContact...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.White),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color(0xFF323244)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (typedMessage.isNotBlank()) {
                        viewModel.sendDirectMessage(selectedContact, typedMessage)
                        typedMessage = ""
                    }
                },
                enabled = typedMessage.isNotBlank(),
                modifier = Modifier
                    .background(if (typedMessage.isNotBlank()) primaryColor else Color(0xFF2B2930), CircleShape)
                    .size(44.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send message",
                    tint = if (typedMessage.isNotBlank()) Color.Black else Color(0xFF6A6A7A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
