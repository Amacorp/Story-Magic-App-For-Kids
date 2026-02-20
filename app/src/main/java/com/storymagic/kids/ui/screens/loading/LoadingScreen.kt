package com.storymagic.kids.ui.screens.loading

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.ui.components.*
import com.storymagic.kids.ui.theme.*
import com.storymagic.kids.ui.viewmodel.LoadingViewModel

@Composable
fun LoadingScreen(
    onStoryGenerated: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLibrary: () -> Unit = {},
    viewModel: LoadingViewModel = hiltViewModel()
) {
    val loadingMessageIndex by viewModel.loadingMessageIndex.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val story by viewModel.story.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(isSuccess, story) {
        if (isSuccess && story != null) {
            LogManager.log("LoadingScreen", "INFO", "Success state detected, story: ${story?.title}")
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            LogManager.log("LoadingScreen", "ERROR", "Error message: $errorMessage")
        }
    }

    AnimatedGradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingMedium),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    isSuccess && story != null -> {
                        SuccessContent(
                            storyTitle = story!!.title,
                            onNavigateToLibrary = {
                                LogManager.log("LoadingScreen", "INFO", "Navigate to Library clicked")
                                onNavigateToLibrary()
                            },
                            onReadNow = {
                                LogManager.log("LoadingScreen", "INFO", "Read now clicked")
                                onStoryGenerated()
                            }
                        )
                    }
                    errorMessage != null -> {
                        ErrorContent(
                            errorMessage = errorMessage!!,
                            onTryAgain = {
                                viewModel.clearError()
                                onNavigateBack()
                            }
                        )
                    }
                    else -> {
                        LoadingContent(
                            loadingMessageIndex = loadingMessageIndex,
                            progress = progress,
                            loadingMessages = viewModel.loadingMessages,
                            onCancel = {
                                viewModel.cancelLoading()
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(
    loadingMessageIndex: Int,
    progress: Float,
    loadingMessages: List<String>,
    onCancel: () -> Unit
) {
    OwlMascot(
        size = 160.dp,
        isActive = true
    )

    Spacer(modifier = Modifier.height(SpacingXLarge))

    AnimatedContent(
        targetState = loadingMessageIndex,
        transitionSpec = {
            slideInVertically { it } + fadeIn() togetherWith
                    slideOutVertically { -it } + fadeOut()
        },
        label = "loadingMessage"
    ) { index ->
        Text(
            text = loadingMessages.getOrElse(index) { "Making magic... ✨" },
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        )
    }

    Spacer(modifier = Modifier.height(SpacingXLarge))

    RoundedProgressBar(
        progress = progress,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(12.dp)
    )

    Spacer(modifier = Modifier.height(SpacingMedium))

    Text(
        text = "This usually takes 15-30 seconds",
        fontSize = 14.sp,
        color = TextSecondary,
        fontWeight = FontWeight.Normal
    )

    Spacer(modifier = Modifier.height(SpacingXLarge * 2))

    TextButton(onClick = onCancel) {
        Text(
            "Cancel",
            color = TextSecondary,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun SuccessContent(
    storyTitle: String,
    onNavigateToLibrary: () -> Unit,
    onReadNow: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Success",
        tint = SuccessGreen,
        modifier = Modifier.size(80.dp)
    )

    Spacer(modifier = Modifier.height(SpacingLarge))

    Text(
        text = "Story Created! 🎉",
        style = MaterialTheme.typography.headlineLarge,
        color = PrimaryCoral,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    )

    Spacer(modifier = Modifier.height(SpacingMedium))

    Text(
        text = "\"$storyTitle\"",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = SpacingLarge)
    )

    Spacer(modifier = Modifier.height(SpacingXLarge))

    Button(
        onClick = onReadNow,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(CornerRadiusLarge),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
    ) {
        Text(
            "Read Now",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(SpacingMedium))

    OutlinedButton(
        onClick = onNavigateToLibrary,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(CornerRadiusLarge),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondarySkyBlue)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.LibraryBooks,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(SpacingSmall))
        Text(
            "Go to Story Library",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onTryAgain: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(SpacingMedium),
        shape = RoundedCornerShape(CornerRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Oops! Something went wrong",
                color = ErrorRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(SpacingMedium))

            Text(
                text = errorMessage,
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(SpacingLarge))

            Button(
                onClick = onTryAgain,
                shape = RoundedCornerShape(CornerRadiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Try Again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RoundedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = EaseInOut),
        label = "animatedProgress"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x20000000))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryCoral, SecondarySkyBlue, TertiarySunshine)
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .fillMaxHeight()
            )
        }
    }
}
