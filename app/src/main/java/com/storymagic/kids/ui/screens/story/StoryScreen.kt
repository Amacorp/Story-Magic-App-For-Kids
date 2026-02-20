package com.storymagic.kids.ui.screens.story

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.ui.components.*
import com.storymagic.kids.ui.theme.*
import com.storymagic.kids.ui.viewmodel.StoryViewModel

@Composable
fun StoryScreen(
    storyId: Int? = null,
    onNavigateBack: () -> Unit,
    onNavigateToNewStory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentStory by viewModel.currentStory.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val showVoiceSelector by viewModel.showVoiceSelector.collectAsState()
    val highlightWords by viewModel.highlightWords.collectAsState()
    val currentWordIndex by viewModel.currentWordIndex.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeTts(context)
        LogManager.log("StoryScreen", "INFO", "Story screen initialized")
        storyId?.let { id ->
            viewModel.loadStoryById(id)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            LogManager.log("StoryScreen", "INFO", "Story screen disposed")
        }
    }

    AnimatedGradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            CelebrationConfetti(
                modifier = Modifier.fillMaxSize(),
                isActive = showCelebration
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                StoryTopBar(
                    onBack = onNavigateBack,
                    onSettings = onNavigateToSettings,
                    onShare = {
                        currentStory?.let { story ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, story.title)
                                putExtra(Intent.EXTRA_TEXT, "${story.title}\n\n${story.storyText}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Story"))
                            viewModel.onShareClicked()
                        }
                    }
                )
                
                currentStory?.let { story ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = SpacingMedium)
                    ) {
                        StoryHeader(story = story)
                        
                        StoryContent(
                            story = story,
                            textSize = viewModel.getTextSizeValue(),
                            highlightWords = highlightWords,
                            currentWordIndex = currentWordIndex,
                            isPlaying = isPlaying
                        )
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ShimmerPlaceholder(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(CornerRadiusLarge))
                            )
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            Text(
                                "Loading your magical story...",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            currentStory?.let {
                StoryPlayerBar(
                    isPlaying = isPlaying,
                    progress = playbackProgress,
                    selectedVoice = selectedVoice,
                    speechRate = speechRate,
                    highlightWords = highlightWords,
                    onPlayPause = viewModel::togglePlayPause,
                    onVoiceClick = viewModel::toggleVoiceSelector,
                    onSpeedClick = viewModel::cycleSpeed,
                    onHighlightClick = { viewModel.setHighlightWords(!highlightWords) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            
            if (showVoiceSelector) {
                VoiceSelectorBottomSheet(
                    voices = viewModel.voiceOptions,
                    selectedVoice = selectedVoice,
                    onVoiceSelect = { 
                        viewModel.setVoice(it)
                        viewModel.toggleVoiceSelector()
                    },
                    onDismiss = viewModel::toggleVoiceSelector
                )
            }
        }
    }
}

@Composable
private fun StoryTopBar(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingSmall, vertical = SpacingXSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(SurfaceWhite.copy(alpha = 0.9f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(SpacingSmall)) {
            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(44.dp)
                    .background(SurfaceWhite.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextPrimary
                )
            }

            IconButton(
                onClick = onSettings,
                modifier = Modifier
                    .size(44.dp)
                    .background(SurfaceWhite.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun StoryHeader(story: com.storymagic.kids.data.local.StoryEntity) {
    val gradientStartColor = try {
        Color(android.graphics.Color.parseColor(story.gradientColorStart))
    } catch (e: Exception) {
        PrimaryCoral
    }
    
    val gradientEndColor = try {
        Color(android.graphics.Color.parseColor(story.gradientColorEnd))
    } catch (e: Exception) {
        SecondarySkyBlue
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .shadow(
                    elevation = CardElevation,
                    shape = RoundedCornerShape(CornerRadiusLarge)
                ),
            shape = RoundedCornerShape(CornerRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                gradientStartColor.copy(alpha = 0.7f),
                                gradientEndColor.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                val genreEmoji = when {
                    story.genres.contains("Magic") -> "\uD83E\uDD84"
                    story.genres.contains("Adventure") -> "\uD83D\uDC09"
                    story.genres.contains("Space") -> "\uD83D\uDE80"
                    story.genres.contains("Animals") -> "\uD83D\uDC3E"
                    story.genres.contains("Fantasy") -> "\uD83E\uDDDA"
                    story.genres.contains("Dinosaurs") -> "\uD83E\uDD95"
                    story.genres.contains("Pirates") -> "\u2620\uFE0F"
                    story.genres.contains("Mystery") -> "\uD83D\uDC7B"
                    else -> "\u2728"
                }
                
                Text(
                    text = genreEmoji,
                    fontSize = 72.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingLarge))
        
        Text(
            text = story.title,
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 26.sp
        )
        
        Spacer(modifier = Modifier.height(SpacingSmall))
        
        Text(
            text = story.description,
            color = TextSecondary,
            fontSize = 15.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(SpacingMedium))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Surface(
                shape = RoundedCornerShape(CornerRadiusSmall),
                color = SurfaceMint
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = SpacingSmall, vertical = SpacingXSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "~${story.readingTimeMinutes} min",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            
            story.moral.let { moral ->
                Surface(
                    shape = RoundedCornerShape(CornerRadiusSmall),
                    color = SurfacePeach
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = SpacingSmall, vertical = SpacingXSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = PrimaryCoral,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            moral,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingLarge))
        
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = TextSecondary.copy(alpha = 0.1f)
        )
        
        Spacer(modifier = Modifier.height(SpacingMedium))
    }
}

@Composable
private fun StoryContent(
    story: com.storymagic.kids.data.local.StoryEntity,
    textSize: Float,
    highlightWords: Boolean,
    currentWordIndex: Int,
    isPlaying: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (highlightWords && isPlaying && currentWordIndex >= 0) {
            val allWords = story.storyText.split(Regex("\\s+")).filter { it.isNotBlank() }
            val paragraphs = story.storyText.split("\n\n")
            var wordCounter = 0
            
            paragraphs.forEach { paragraph ->
                if (paragraph.isNotBlank()) {
                    val paragraphWords = paragraph.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                    val annotatedString = buildAnnotatedString {
                        paragraphWords.forEachIndexed { index, word ->
                            val globalIndex = wordCounter + index
                            if (globalIndex == currentWordIndex) {
                                withStyle(
                                    SpanStyle(
                                        color = TextPrimary,
                                        background = TertiarySunshine.copy(alpha = 0.4f),
                                        fontWeight = FontWeight.Medium
                                    )
                                ) {
                                    append(word)
                                }
                            } else {
                                withStyle(SpanStyle(color = TextPrimary)) {
                                    append(word)
                                }
                            }
                            if (index < paragraphWords.size - 1) {
                                append(" ")
                            }
                        }
                    }
                    wordCounter += paragraphWords.size
                    
                    Text(
                        text = annotatedString,
                        fontSize = textSize.sp,
                        lineHeight = (textSize * 1.8).sp,
                        modifier = Modifier.padding(bottom = SpacingMedium)
                    )
                }
            }
        } else {
            val paragraphs = story.storyText.split("\n\n")
            paragraphs.forEach { paragraph ->
                if (paragraph.isNotBlank()) {
                    Text(
                        text = paragraph.trim(),
                        color = TextPrimary,
                        fontSize = textSize.sp,
                        lineHeight = (textSize * 1.8).sp,
                        modifier = Modifier.padding(bottom = SpacingMedium)
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryPlayerBar(
    isPlaying: Boolean,
    progress: Float,
    selectedVoice: String,
    speechRate: Float,
    highlightWords: Boolean,
    onPlayPause: () -> Unit,
    onVoiceClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onHighlightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = isPlaying,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = PrimaryCoral,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingMedium, horizontal = SpacingMedium),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerButton(
                    icon = if (highlightWords) Icons.Default.FormatUnderlined else Icons.Default.FormatUnderlined,
                    label = "Highlight",
                    isActive = highlightWords,
                    onClick = onHighlightClick
                )
                
                PlayerButton(
                    icon = Icons.Default.RecordVoiceOver,
                    label = selectedVoice.split(" ").first(),
                    isActive = false,
                    onClick = onVoiceClick
                )
                
                FloatingActionButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(64.dp),
                    containerColor = PrimaryCoral,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                PlayerButton(
                    icon = Icons.Default.Speed,
                    label = "${speechRate}x",
                    isActive = false,
                    onClick = onSpeedClick
                )
                
                PlayerButton(
                    icon = Icons.Default.SkipNext,
                    label = "Skip",
                    isActive = false,
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun PlayerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = if (isActive) PrimaryCoral else SurfaceMint
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isActive) Color.White else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun VoiceSelectorBottomSheet(
    voices: List<String>,
    selectedVoice: String,
    onVoiceSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = CornerRadiusXLarge, topEnd = CornerRadiusXLarge),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingLarge)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(SpacingMedium))
                
                Text(
                    "Choose a Voice",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(SpacingMedium))
                
                voices.forEach { voice ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVoiceSelect(voice) },
                        shape = RoundedCornerShape(CornerRadiusMedium),
                        color = if (voice == selectedVoice) SurfaceMint else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SpacingMedium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = PrimaryCoral.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            when {
                                                voice.contains("Mom") || voice.contains("Sister") || voice.contains("Grandma") -> Icons.Default.Face
                                                voice.contains("Teacher") -> Icons.Default.School
                                                else -> Icons.Default.Person
                                            },
                                            contentDescription = null,
                                            tint = PrimaryCoral,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(SpacingMedium))
                                Text(voice, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                            if (voice == selectedVoice) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(SpacingMedium))
            }
        }
    }
}
