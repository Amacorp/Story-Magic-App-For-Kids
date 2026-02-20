package com.storymagic.kids.ui.screens.library

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.storymagic.kids.data.local.StoryEntity
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.ui.components.*
import com.storymagic.kids.ui.theme.*
import com.storymagic.kids.ui.viewmodel.LibraryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToStory: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val filteredStories by viewModel.filteredStories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    
    LaunchedEffect(Unit) {
        LogManager.log("LibraryScreen", "INFO", "Library screen opened")
    }
    
    AnimatedGradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "My Stories",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    placeholder = { Text("Search stories...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingMedium),
                    shape = RoundedCornerShape(CornerRadiusXLarge),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { }),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCoral,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                    )
                )
                
                Spacer(modifier = Modifier.height(SpacingMedium))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = SpacingMedium),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
                ) {
                    viewModel.filterOptions.forEach { filter ->
                        FilterChip(
                            selected = filter == selectedFilter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter) },
                            shape = RoundedCornerShape(CornerRadiusMedium),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryCoral,
                                selectedLabelColor = SurfaceWhite
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(SpacingMedium))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryCoral)
                    }
                } else if (filteredStories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(SpacingLarge)
                        ) {
                            OwlMascot(size = 120.dp)
                            Spacer(modifier = Modifier.height(SpacingLarge))
                            Text(
                                "No stories yet!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(SpacingSmall))
                            Text(
                                "Create your first magical story!",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(SpacingLarge))
                            Button(
                                onClick = onNavigateToCreate,
                                shape = RoundedCornerShape(CornerRadiusLarge),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral),
                                modifier = Modifier.height(LargeTouchTarget)
                            ) {
                                Text("Create Story", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = SpacingMedium),
                        horizontalArrangement = Arrangement.spacedBy(SpacingMedium),
                        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
                    ) {
                        items(filteredStories, key = { it.id }) { story ->
                            StoryCard(
                                story = story,
                                onClick = { onNavigateToStory(story.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(story) },
                                onDeleteRequest = { viewModel.showDeleteConfirmation(story) }
                            )
                        }
                    }
                }
            }
            
            showDeleteConfirmation?.let { story ->
                DeleteConfirmationDialog(
                    storyTitle = story.title,
                    onConfirm = { viewModel.deleteStory(story) },
                    onDismiss = { viewModel.hideDeleteConfirmation() }
                )
            }
        }
    }
}

@Composable
private fun StoryCard(
    story: StoryEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    
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
    
    Card(
        modifier = Modifier
            .aspectRatio(3f / 4f)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(CornerRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = CornerRadiusLarge,
                            topEnd = CornerRadiusLarge,
                            bottomEnd = CornerRadiusSmall,
                            bottomStart = CornerRadiusSmall
                        )
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                gradientStartColor.copy(alpha = 0.7f),
                                gradientEndColor.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SpacingSmall)
                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(CornerRadiusSmall))
                ) {
                    Icon(
                        if (story.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (story.isFavorite) "Unfavorite" else "Favorite",
                        tint = if (story.isFavorite) TertiarySunshine else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
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
                    fontSize = 32.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(SpacingSmall)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingMedium)
            ) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(SpacingXSmall))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(story.createdAt),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(CornerRadiusSmall),
                        color = SurfaceLavender
                    ) {
                        Text(
                            text = "~${story.readingTimeMinutes} min",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = SpacingXSmall, vertical = SpacingXSmall)
                        )
                    }
                }
            }
            
            IconButton(
                onClick = onDeleteRequest,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = SpacingMedium, bottom = SpacingSmall)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    storyTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(CornerRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingLarge)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Delete Story?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(SpacingMedium))
                
                Text(
                    "Are you sure you want to delete \"$storyTitle\"? This cannot be undone.",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(SpacingLarge))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadiusMedium)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadiusMedium),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
