package com.storymagic.kids.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.storymagic.kids.ui.theme.*

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )
    
    val colors = listOf(
        GradientPink,
        GradientBlue,
        GradientMint,
        GradientPeach,
        GradientPurple
    )
    
    val startIndex = (colorShift * (colors.size - 1)).toInt()
    val endIndex = minOf(startIndex + 1, colors.size - 1)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors[startIndex],
                        colors[endIndex],
                        colors[(endIndex + 1) % colors.size]
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        content()
    }
}

@Composable
fun FloatingElement(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    duration: Int = 4000,
    element: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration * 3,
                delayMillis = delay,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .rotate(rotation)
    ) {
        element()
    }
}

@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CornerRadiusLarge,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.85f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pressedScale: Float = 0.92f,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .background(
                color = SurfaceWhite,
                shape = RoundedCornerShape(CornerRadiusMedium)
            )
            .clickable(
                onClick = onClick,
                enabled = enabled
            )
    ) {
        content()
    }
}

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CornerRadiusSmall
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE8E8E8),
            Color(0xFFF8F8F8),
            Color(0xFFE8E8E8)
        ),
        start = Offset(shimmerOffset * 200f, 0f),
        end = Offset((shimmerOffset + 1) * 200f, 0f)
    )
    
    Box(
        modifier = modifier
            .background(
                brush = shimmerBrush,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
    )
}

@Composable
fun CelebrationConfetti(
    modifier: Modifier = Modifier,
    isActive: Boolean
) {
    if (!isActive) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    
    Box(modifier = modifier.fillMaxSize()) {
        repeat(25) { index ->
            val colors = listOf(RainbowRed, RainbowOrange, RainbowYellow, RainbowGreen, RainbowBlue, RainbowPurple, RainbowPink)
            val color = colors[index % colors.size]
            
            val offsetX by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = (index % 7 - 3) * 100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000 + index * 100,
                        easing = EaseOut
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "confettiX$index"
            )
            
            val offsetY by infiniteTransition.animateFloat(
                initialValue = -30f,
                targetValue = 700f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000 + index * 100,
                        easing = EaseOut
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "confettiY$index"
            )
            
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500 + index * 50),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation$index"
            )
            
            Box(
                modifier = Modifier
                    .offset(
                        x = offsetX.dp,
                        y = offsetY.dp
                    )
                    .rotate(rotation)
                    .size(if (index % 3 == 0) 10.dp else 6.dp)
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

@Composable
fun RainbowBorderCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        RainbowRed,
                        RainbowOrange,
                        RainbowYellow,
                        RainbowGreen,
                        RainbowBlue,
                        RainbowPurple,
                        RainbowPink,
                        RainbowRed
                    )
                ),
                shape = RoundedCornerShape(CornerRadiusLarge)
            )
            .padding(3.dp)
            .background(
                color = SurfaceWhite,
                shape = RoundedCornerShape(CornerRadiusLarge - 3.dp)
            )
    ) {
        content()
    }
}
