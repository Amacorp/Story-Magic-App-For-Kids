package com.storymagic.kids.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.ui.components.*
import com.storymagic.kids.ui.theme.*
import com.storymagic.kids.ui.viewmodel.OnboardingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToLoading: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val childName by viewModel.childName.collectAsState()
    val childAge by viewModel.childAge.collectAsState()
    val childGender by viewModel.childGender.collectAsState()
    val selectedGenres by viewModel.selectedGenres.collectAsState()
    val belovedObject by viewModel.belovedObject.collectAsState()
    val includePet by viewModel.includePet.collectAsState()
    val selectedPetType by viewModel.selectedPetType.collectAsState()
    val petName by viewModel.petName.collectAsState()
    val selectedMoral by viewModel.selectedMoral.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val navigateToLoading by viewModel.navigateToLoading.collectAsState()

    LaunchedEffect(Unit) {
        LogManager.log("OnboardingScreen", "INFO", "OnboardingScreen loaded - resetting state")
        viewModel.resetState()
    }

    LaunchedEffect(navigateToLoading) {
        if (navigateToLoading) {
            LogManager.log("OnboardingScreen", "INFO", "Navigating to loading screen...")
            viewModel.onNavigatedToLoading()
            onNavigateToLoading()
        }
    }
    
    AnimatedGradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingMedium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingMedium),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 12.dp else 8.dp)
                                .background(
                                    color = if (index <= currentStep) PrimaryCoral else TextSecondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        if (index < 3) {
                            Spacer(modifier = Modifier.width(SpacingSmall))
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "stepAnimation"
                    ) { step ->
                        when (step) {
                            0 -> Step1NameAndGender(
                                name = childName,
                                gender = childGender,
                                onNameChange = viewModel::setChildName,
                                onGenderChange = viewModel::setChildGender,
                                onNext = viewModel::nextStep
                            )
                            1 -> Step2Age(
                                age = childAge,
                                onAgeSelect = viewModel::setChildAge,
                                onNext = viewModel::nextStep,
                                onBack = viewModel::previousStep
                            )
                            2 -> Step3Preferences(
                                selectedGenres = selectedGenres,
                                belovedObject = belovedObject,
                                includePet = includePet,
                                selectedPetType = selectedPetType,
                                petName = petName,
                                onGenreToggle = viewModel::toggleGenre,
                                onBelovedObjectChange = viewModel::setBelovedObject,
                                onIncludePetChange = viewModel::setIncludePet,
                                onPetTypeSelect = viewModel::setPetType,
                                onPetNameChange = viewModel::setPetName,
                                onNext = viewModel::nextStep,
                                onBack = viewModel::previousStep
                            )
                            3 -> Step4Moral(
                                selectedMoral = selectedMoral,
                                onMoralSelect = viewModel::setMoral,
                                onCreateStory = {
                                    LogManager.log("OnboardingScreen", "INFO", "=== CREATE MY STORY BUTTON CLICKED ===")
                                    viewModel.prepareStoryGeneration()
                                },
                                onBack = viewModel::previousStep
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1NameAndGender(
    name: String,
    gender: String,
    onNameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MagicalMascot(
            size = 140.dp,
            isActive = true
        )
        
        Spacer(modifier = Modifier.height(SpacingXLarge))
        
        Text(
            text = "What's your name, little explorer?",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(SpacingLarge))
        
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Enter your name") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerRadiusXLarge)),
            shape = RoundedCornerShape(CornerRadiusXLarge),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryCoral,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            maxLines = 1
        )
        
        Spacer(modifier = Modifier.height(SpacingLarge))
        
        Text(
            text = "Are you a boy or a girl?",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(SpacingMedium))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            GenderOption(
                label = "Boy",
                icon = Icons.Default.Face,
                selected = gender == "Boy",
                onClick = { onGenderChange("Boy") },
                modifier = Modifier.weight(1f)
            )
            GenderOption(
                label = "Girl",
                icon = Icons.Default.Face,
                selected = gender == "Girl",
                onClick = { onGenderChange("Girl") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingXLarge))
        
        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier
                .height(LargeTouchTarget)
                .fillMaxWidth(0.8f),
            shape = RoundedCornerShape(CornerRadiusLarge),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (name.isNotBlank()) PrimaryCoral else TextSecondary.copy(alpha = 0.3f)
            )
        ) {
            Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GenderOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadiusLarge),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) PrimaryCoral else SurfaceWhite
        ),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(2.dp, TextSecondary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else PrimaryCoral,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(SpacingSmall))
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
private fun Step2Age(
    age: Int,
    onAgeSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val ageEmojis = mapOf(
        3 to "🧸", 4 to "🎨", 5 to "🌟", 6 to "🦋", 7 to "🚀",
        8 to "🎵", 9 to "📚", 10 to "🔬", 11 to "🌍", 12 to "🎭"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How old are you?",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(SpacingXLarge))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(10) { index ->
                val ageValue = index + 3
                val isSelected = age == ageValue

                FilterChip(
                    selected = isSelected,
                    onClick = { onAgeSelect(ageValue) },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(ageValue.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(ageEmojis[ageValue] ?: "", fontSize = 16.sp)
                        }
                    },
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(CornerRadiusMedium),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryCoral,
                        selectedLabelColor = SurfaceWhite
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingXLarge))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(MinTouchTarget)
                    .background(SurfaceWhite, RoundedCornerShape(CornerRadiusMedium))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier
                    .height(LargeTouchTarget)
                    .weight(1f),
                shape = RoundedCornerShape(CornerRadiusLarge),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
            ) {
                Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun Step3Preferences(
    selectedGenres: List<String>,
    belovedObject: String,
    includePet: Boolean,
    selectedPetType: String?,
    petName: String,
    onGenreToggle: (String) -> Unit,
    onBelovedObjectChange: (String) -> Unit,
    onIncludePetChange: (Boolean) -> Unit,
    onPetTypeSelect: (String?) -> Unit,
    onPetNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val genres = listOf(
        "Adventure 🐉", "Fantasy 🧚", "Space 🚀", "Dinosaurs 🦕",
        "Magic 🦄", "Animals 🐾", "Pirates 🏴‍☠️", "Mystery 👻"
    )
    
    val petTypes = listOf("Dog 🐕", "Cat 🐱", "Rabbit 🐰", "Bird 🐦", "Dragon 🐲", "Robot 🤖")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingMedium)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What kind of story?",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(SpacingMedium))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            genres.forEach { genre ->
                FilterChip(
                    selected = genre in selectedGenres,
                    onClick = { onGenreToggle(genre) },
                    label = { Text(genre) },
                    shape = RoundedCornerShape(CornerRadiusMedium),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryCoral,
                        selectedLabelColor = SurfaceWhite
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingLarge))
        
        OutlinedTextField(
            value = belovedObject,
            onValueChange = onBelovedObjectChange,
            label = { Text("What special thing should be in the story?") },
            placeholder = { Text("e.g., red bicycle, teddy bear, magic blanket") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerRadiusXLarge)),
            shape = RoundedCornerShape(CornerRadiusXLarge),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryCoral,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
            ),
            maxLines = 2
        )
        
        Spacer(modifier = Modifier.height(SpacingLarge))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Include a pet friend?",
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Switch(
                    checked = includePet,
                    onCheckedChange = onIncludePetChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SurfaceWhite,
                        checkedTrackColor = PrimaryCoral
                    )
                )
            }
        }
        
        if (includePet) {
            Spacer(modifier = Modifier.height(SpacingMedium))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                petTypes.forEach { pet ->
                    FilterChip(
                        selected = selectedPetType == pet,
                        onClick = { onPetTypeSelect(if (selectedPetType == pet) null else pet) },
                        label = { Text(pet) },
                        shape = RoundedCornerShape(CornerRadiusMedium),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondarySkyBlue,
                            selectedLabelColor = SurfaceWhite
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(SpacingMedium))
            
            OutlinedTextField(
                value = petName,
                onValueChange = onPetNameChange,
                label = { Text("Pet's name") },
                placeholder = { Text("e.g., Buddy, Whiskers") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerRadiusXLarge)),
                shape = RoundedCornerShape(CornerRadiusXLarge),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryCoral,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                ),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingLarge))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(MinTouchTarget)
                    .background(SurfaceWhite, RoundedCornerShape(CornerRadiusMedium))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier
                    .height(LargeTouchTarget)
                    .weight(1f),
                shape = RoundedCornerShape(CornerRadiusLarge),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
            ) {
                Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step4Moral(
    selectedMoral: String,
    onMoralSelect: (String) -> Unit,
    onCreateStory: () -> Unit,
    onBack: () -> Unit
) {
    val morals = listOf("Friendship", "Courage", "Sharing", "Kindness", "Honesty", "Curiosity", "Teamwork")
    
    var isCreating by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "What should the story teach?",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(SpacingXLarge))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            morals.forEach { moral ->
                FilterChip(
                    selected = selectedMoral == moral,
                    onClick = { onMoralSelect(moral) },
                    label = { Text(moral) },
                    shape = RoundedCornerShape(CornerRadiusMedium),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SuccessGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingXLarge * 2))

        Button(
            onClick = {
                LogManager.log("OnboardingScreen", "INFO", "=== CREATE MY STORY BUTTON CLICKED ===")
                isCreating = true
                onCreateStory()
            },
            enabled = !isCreating,
            modifier = Modifier
                .height(LargeTouchTarget)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(CornerRadiusLarge),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = SurfaceWhite
                )
                Spacer(modifier = Modifier.width(SpacingMedium))
            }
            Text(
                text = "Create My Story! ✨",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingMedium))
        
        TextButton(onClick = onBack) {
            Text("Back", color = TextSecondary)
        }
    }
}
