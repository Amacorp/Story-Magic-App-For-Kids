package com.storymagic.kids.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.ui.components.AnimatedGradientBackground
import com.storymagic.kids.ui.theme.*
import com.storymagic.kids.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val apiKey by viewModel.apiKey.collectAsState()
    val isApiKeyVisible by viewModel.isApiKeyVisible.collectAsState()
    val connectionTestResult by viewModel.connectionTestResult.collectAsState()
    val selectedStoryModel by viewModel.selectedStoryModel.collectAsState()
    val creativity by viewModel.creativity.collectAsState()
    val fallbackModel by viewModel.fallbackModel.collectAsState()
    val defaultVoice by viewModel.defaultVoice.collectAsState()
    val highlightWords by viewModel.highlightWords.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val autoPlay by viewModel.autoPlay.collectAsState()
    val saveStories by viewModel.saveStories.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val hasPin by viewModel.hasPin.collectAsState()
    val dailyLimit by viewModel.dailyLimit.collectAsState()
    val contentFilter by viewModel.contentFilter.collectAsState()
    val logEntries by viewModel.logEntries.collectAsState()
    
    var showPinInputDialog by remember { mutableStateOf(false) }
    var isParentalUnlocked by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadApiKey()
        viewModel.updateLogCount()
        LogManager.log("SettingsScreen", "INFO", "Settings screen opened")
    }
    
    AnimatedGradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
                
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = SpacingMedium),
                    verticalArrangement = Arrangement.spacedBy(SpacingMedium)
                ) {
                    item {
                        SettingsSection(title = "API Configuration", icon = Icons.Default.Key) {
                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = viewModel::setApiKey,
                                label = { Text("OpenRouter API Key") },
                                visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(CornerRadiusMedium),
                                trailingIcon = {
                                    IconButton(onClick = viewModel::toggleApiKeyVisibility) {
                                        Icon(
                                            if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isApiKeyVisible) "Hide" else "Show"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryCoral,
                                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                            ) {
                                Button(
                                    onClick = viewModel::testConnection,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(CornerRadiusMedium),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondarySkyBlue)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(SpacingXSmall))
                                    Text("Test Connection")
                                }
                                
                                connectionTestResult?.let { result ->
                                    Icon(
                                        if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (result.success) SuccessGreen else ErrorRed,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            
                            if (connectionTestResult != null) {
                                Spacer(modifier = Modifier.height(SpacingSmall))
                                Text(
                                    connectionTestResult!!.message,
                                    color = if (connectionTestResult!!.success) SuccessGreen else ErrorRed,
                                    fontSize = 14.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            val uriHandler = LocalUriHandler.current
                            Text(
                                "Get your API key at openrouter.ai/keys",
                                color = PrimaryCoral,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    uriHandler.openUri("https://openrouter.ai/keys")
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Button(
                                onClick = viewModel::saveApiKey,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(CornerRadiusMedium),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                            ) {
                                Text("Save API Key", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    item {
                        SettingsSection(title = "Story Model", icon = Icons.Default.Build) {
                            var storyModelExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = storyModelExpanded,
                                onExpandedChange = { storyModelExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedStoryModel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Story Model") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storyModelExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(CornerRadiusMedium)
                                )
                                ExposedDropdownMenu(
                                    expanded = storyModelExpanded,
                                    onDismissRequest = { storyModelExpanded = false }
                                ) {
                                    viewModel.storyModelOptions.forEach { model ->
                                        DropdownMenuItem(
                                            text = { Text(model) },
                                            onClick = {
                                                viewModel.setStoryModel(model)
                                                storyModelExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Text("Creativity (Temperature): ${String.format("%.1f", creativity)}", fontWeight = FontWeight.Medium)
                            Slider(
                                value = creativity,
                                onValueChange = viewModel::setCreativity,
                                valueRange = 0.5f..1.0f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Fallback Model", fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = fallbackModel,
                                    onCheckedChange = viewModel::setFallbackModel,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SurfaceWhite,
                                        checkedTrackColor = PrimaryCoral
                                    )
                                )
                            }
                        }
                    }
                    
                    item {
                        SettingsSection(title = "Voice Settings", icon = Icons.Default.RecordVoiceOver) {
                            viewModel.voiceOptions.forEach { voice ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setDefaultVoice(voice) }
                                        .padding(vertical = SpacingSmall),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(voice, fontWeight = if (voice == defaultVoice) FontWeight.Bold else FontWeight.Normal)
                                    RadioButton(
                                        selected = voice == defaultVoice,
                                        onClick = { viewModel.setDefaultVoice(voice) },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryCoral)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Highlight Words While Reading", fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = highlightWords,
                                    onCheckedChange = viewModel::setHighlightWords,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SurfaceWhite,
                                        checkedTrackColor = PrimaryCoral
                                    )
                                )
                            }
                        }
                    }
                    
                    item {
                        SettingsSection(title = "App Preferences", icon = Icons.Default.Settings) {
                            Text("Text Size", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(SpacingSmall))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
                            ) {
                                viewModel.textSizeOptions.forEach { size ->
                                    FilterChip(
                                        selected = size == textSize,
                                        onClick = { viewModel.setTextSize(size) },
                                        label = { Text(size) },
                                        shape = RoundedCornerShape(CornerRadiusSmall)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auto-play Stories", fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = autoPlay,
                                    onCheckedChange = viewModel::setAutoPlay,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SurfaceWhite,
                                        checkedTrackColor = PrimaryCoral
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Save All Stories", fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = saveStories,
                                    onCheckedChange = viewModel::setSaveStories,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SurfaceWhite,
                                        checkedTrackColor = PrimaryCoral
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Text("Theme", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(SpacingSmall))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
                            ) {
                                viewModel.themeOptions.forEach { t ->
                                    FilterChip(
                                        selected = t == theme,
                                        onClick = { viewModel.setTheme(t) },
                                        label = { Text(t) },
                                        shape = RoundedCornerShape(CornerRadiusSmall),
                                        enabled = t == "Light"
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        SettingsSection(title = "Parental Controls", icon = Icons.Default.Lock) {
                            if (!hasPin) {
                                Button(
                                    onClick = { showPinInputDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(CornerRadiusMedium)
                                ) {
                                    Text("Set PIN")
                                }
                            } else {
                                Button(
                                    onClick = { showPinInputDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(CornerRadiusMedium),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondarySkyBlue)
                                ) {
                                    Text(if (isParentalUnlocked) "Change PIN" else "Enter PIN")
                                }
                            }
                            
                            if (isParentalUnlocked || !hasPin) {
                                Spacer(modifier = Modifier.height(SpacingMedium))
                                
                                Text("Daily Story Limit: $dailyLimit", fontWeight = FontWeight.Medium)
                                Slider(
                                    value = dailyLimit.toFloat(),
                                    onValueChange = { viewModel.setDailyLimit(it.toInt().toString()) },
                                    valueRange = 1f..20f,
                                    steps = 18,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(SpacingMedium))
                                
                                Text("Content Filter", fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(SpacingSmall))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
                                ) {
                                    viewModel.contentFilterOptions.forEach { filter ->
                                        FilterChip(
                                            selected = filter == contentFilter,
                                            onClick = { viewModel.setContentFilter(filter) },
                                            label = { Text(filter) },
                                            shape = RoundedCornerShape(CornerRadiusSmall)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        SettingsSection(title = "App Debug Log", icon = Icons.Default.BugReport) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$logEntries entries", color = TextSecondary, fontSize = 14.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSmall)) {
                                    TextButton(onClick = { viewModel.copyLogs(context) }) {
                                        Text("Copy Logs")
                                    }
                                    TextButton(onClick = viewModel::clearLogs) {
                                        Text("Clear Logs")
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(SpacingSmall))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(Color(0xFF1A1A2E), RoundedCornerShape(CornerRadiusMedium))
                                    .padding(SpacingMedium)
                            ) {
                                val formattedLogs = LogManager.getFormattedLogs()
                                if (formattedLogs.isEmpty()) {
                                    Text(
                                        "No logs yet. Logs will appear here as you use the app.",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Column {
                                        LogManager.logs.forEach { entry ->
                                            val levelColor = when (entry.level) {
                                                "DEBUG" -> LogDebugColor
                                                "INFO" -> LogInfoColor
                                                "WARN" -> LogWarnColor
                                                "ERROR" -> LogErrorColor
                                                else -> Color.White
                                            }
                                            Text(
                                                text = "[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))}] [${entry.level}] [${entry.tag}] ${entry.message}",
                                                color = levelColor,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(SpacingMedium))
                            
                            Button(
                                onClick = {
                                    viewModel.shareLogs(context)?.let { intent ->
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share Logs"))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(CornerRadiusMedium),
                                colors = ButtonDefaults.buttonColors(containerColor = TertiarySunshine)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(SpacingXSmall))
                                Text("Share Logs", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    item {
                        SettingsSection(title = "About", icon = Icons.Default.Info) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "StoryMagic Kids",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCoral
                                )
                                Spacer(modifier = Modifier.height(SpacingXSmall))
                                Text("Version 1.0.0", color = TextSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(SpacingSmall))
                                Text(
                                    "AI-powered storytelling for children ages 3-12",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = SpacingLarge)
                                )
                                
                                Spacer(modifier = Modifier.height(SpacingLarge))
                                
                                Text(
                                    "Follow us on:",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                                
                                Spacer(modifier = Modifier.height(SpacingMedium))
                                
                                val uriHandler = LocalUriHandler.current
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                uriHandler.openUri("https://github.com/Amacorp")
                                            } catch (e: Exception) {
                                                LogManager.log("SettingsScreen", "ERROR", "Failed to open GitHub: ${e.message}")
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(TextPrimary.copy(alpha = 0.1f), RoundedCornerShape(CornerRadiusMedium))
                                    ) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(com.storymagic.kids.R.drawable.ic_github),
                                            contentDescription = "GitHub",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(SpacingMedium))
                                    
                                    IconButton(
                                        onClick = {
                                            try {
                                                uriHandler.openUri("https://www.linkedin.com/in/m-gakhramanian/")
                                            } catch (e: Exception) {
                                                LogManager.log("SettingsScreen", "ERROR", "Failed to open LinkedIn: ${e.message}")
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(PrimaryCoral.copy(alpha = 0.1f), RoundedCornerShape(CornerRadiusMedium))
                                    ) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(com.storymagic.kids.R.drawable.ic_linkedin),
                                            contentDescription = "LinkedIn",
                                            tint = PrimaryCoral,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(SpacingLarge))
                                
                                OutlinedButton(
                                    onClick = { showPrivacyDialog = true },
                                    shape = RoundedCornerShape(CornerRadiusMedium)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(SpacingXSmall))
                                    Text("Privacy Policy")
                                }
                                
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(SpacingXLarge))
                    }
                }
            }
        }
    }
    
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    
    if (showPinInputDialog) {
        PinInputDialog(
            hasExistingPin = hasPin,
            viewModel = viewModel,
            onPinSet = { pin ->
                viewModel.setPin(pin)
                showPinInputDialog = false
                isParentalUnlocked = true
            },
            onPinVerified = { isValid ->
                if (isValid) {
                    isParentalUnlocked = true
                }
                showPinInputDialog = false
            },
            onDismiss = { showPinInputDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = PrimaryCoral, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(SpacingSmall))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(SpacingMedium))
            content()
        }
    }
}

@Composable
private fun PinInputDialog(
    hasExistingPin: Boolean,
    viewModel: SettingsViewModel,
    onPinSet: (String) -> Unit,
    onPinVerified: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

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
                Text(
                    if (hasExistingPin) "Enter PIN" else "Set PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(SpacingMedium))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CornerRadiusMedium),
                    singleLine = true,
                    maxLines = 1
                )

                if (!hasExistingPin) {
                    Spacer(modifier = Modifier.height(SpacingMedium))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it },
                        label = { Text("Confirm PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(CornerRadiusMedium),
                        singleLine = true,
                        maxLines = 1
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(SpacingSmall))
                    Text(error!!, color = ErrorRed, fontSize = 14.sp)
                }

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
                        onClick = {
                            if (hasExistingPin) {
                                if (viewModel.verifyPin(pin)) {
                                    onPinVerified(true)
                                } else {
                                    error = "Incorrect PIN"
                                }
                            } else {
                                when {
                                    pin.length < 4 -> error = "PIN must be at least 4 digits"
                                    pin != confirmPin -> error = "PINs do not match"
                                    else -> onPinSet(pin)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadiusMedium),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                    ) {
                        Text(if (hasExistingPin) "Unlock" else "Set PIN")
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(CornerRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SpacingLarge)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Privacy Policy",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCoral
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(SpacingMedium))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "StoryMagic Kids provides a safe, AI-powered storytelling experience for children ages 3-12. We prioritize privacy through a \"local-first\" data model.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(SpacingLarge))
                    
                    Text(
                        "1. Data Collection & AI",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(SpacingSmall))
                    
                    Text(
                        "• Personalization: We use the child's name, age, gender, and interests (e.g., dinosaurs, magic) solely to generate age-appropriate stories.\n\n• AI Processing: Prompts are sent to OpenRouter (Gemini, DeepSeek, or Llama) to craft narratives.\n\n• No Tracking: We do not use analytics, tracking, or advertisements.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(SpacingLarge))
                    
                    Text(
                        "2. Security & Storage",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(SpacingSmall))
                    
                    Text(
                        "• Local Storage: All stories and profiles stay only on your device.\n\n• Encryption: Your OpenRouter API key is secured using EncryptedSharedPreferences.\n\n• Full Control: Parents can manage stories via PIN-protected controls or erase all data by uninstalling the app.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(SpacingMedium))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CornerRadiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
