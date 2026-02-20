package com.storymagic.kids.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.storymagic.kids.R
import kotlinx.coroutines.delay

private data class MascotInfo(
    val drawableRes: Int,
    val name: String
)

private val mascots = listOf(
    MascotInfo(R.drawable.mascot_rocket, "Rocket"),
    MascotInfo(R.drawable.mascot_car, "Car"),
    MascotInfo(R.drawable.mascot_fire, "Fire"),
    MascotInfo(R.drawable.mascot_leaf, "Leaf"),
    MascotInfo(R.drawable.mascot_clock, "Clock"),
    MascotInfo(R.drawable.mascot_palette, "Palette"),
    MascotInfo(R.drawable.mascot_lab, "Lab"),
    MascotInfo(R.drawable.mascot_ribbon, "Ribbon")
)

@Composable
fun MagicalMascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    isActive: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot")
    
    var currentMascotIndex by remember { mutableIntStateOf((0..7).random()) }
    var nextMascotIndex by remember { mutableIntStateOf((currentMascotIndex + 1) % mascots.size) }
    var transitionProgress by remember { mutableFloatStateOf(0f) }
    var isTransitioning by remember { mutableStateOf(false) }
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                delay(4000)
                isTransitioning = true
                transitionProgress = 0f
                
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing)
                ) { value, _ ->
                    transitionProgress = value
                }
                
                currentMascotIndex = nextMascotIndex
                nextMascotIndex = (nextMascotIndex + 1) % mascots.size
                isTransitioning = false
            }
        }
    }
    
    val currentAlpha = if (isTransitioning) 1f - transitionProgress else alpha
    val nextAlpha = if (isTransitioning) transitionProgress else 0f
    
    androidx.compose.foundation.layout.Box(
        modifier = modifier.size(size)
    ) {
        if (isTransitioning && nextAlpha > 0.01f) {
            Image(
                painter = painterResource(mascots[nextMascotIndex].drawableRes),
                contentDescription = mascots[nextMascotIndex].name,
                modifier = Modifier
                    .size(size * scale)
                    .matchParentSize(),
                alpha = nextAlpha
            )
        }
        
        Image(
            painter = painterResource(mascots[currentMascotIndex].drawableRes),
            contentDescription = mascots[currentMascotIndex].name,
            modifier = Modifier
                .size(size * scale)
                .matchParentSize(),
            alpha = currentAlpha
        )
    }
}

@Composable
fun OwlMascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    isWaving: Boolean = false,
    isActive: Boolean = false
) {
    MagicalMascot(
        modifier = modifier,
        size = size,
        isActive = isActive || isWaving
    )
}
