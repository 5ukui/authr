package com.sukui.authenticator.ui.component.pinboard

import android.annotation.SuppressLint
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sukui.authenticator.ui.component.Animatable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun PrimaryPinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: PinButtonColors = PinButtonDefaults.primaryPinButtonColors(),
    shapes: PinButtonShapes = PinButtonDefaults.plainPinButtonShapes(),
    content: @Composable () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val buttonSize = (screenWidth / 5.2).dp
    PinButton(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.size(buttonSize),
        enabled = enabled,
        colors = colors,
        shapes = shapes,
        content = content
    )
}

@Composable
fun PinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: PinButtonColors = PinButtonDefaults.plainPinButtonColors(),
    shapes: PinButtonShapes = PinButtonDefaults.plainPinButtonShapes(),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape by shapes.getButtonShape(interactionSource)
    val backgroundColor by colors.getBackgroundColor(interactionSource)
    val contentColor by colors.getForegroundColor(interactionSource)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .sizeIn(
                minWidth = PinButtonDefaults.PinButtonMinSize,
                minHeight = PinButtonDefaults.PinButtonMinSize,
            )
            .graphicsLayer {
                clip = true
                this.shape = shape
            }
            .drawBehind {
                drawRect(backgroundColor)
            }
            .combinedClickable(
                onClick = onClick,
                enabled = enabled,
                indication = null,
                interactionSource = interactionSource,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineLarge,
            LocalContentColor provides contentColor,
            content = content
        )
    }
}

object PinButtonDefaults {

    val PinButtonMinSize = 72.dp
    const val AnimationDurationPress = 0
    const val AnimationDurationRelease = 0

    @Composable
    fun plainPinButtonColors(
        backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        backgroundColorPressed: Color = MaterialTheme.colorScheme.primary,
        foregroundColor: Color = MaterialTheme.colorScheme.onSurface,
        foregroundColorPressed: Color = MaterialTheme.colorScheme.onPrimary
    ): PinButtonColors {
        return PinButtonColors(
            backgroundColor = backgroundColor,
            backgroundColorPressed = backgroundColorPressed,
            foregroundColor = foregroundColor,
            foregroundColorPressed = foregroundColorPressed
        )
    }

    @Composable
    fun primaryPinButtonColors(
        backgroundColor: Color = MaterialTheme.colorScheme.secondary,
        backgroundColorPressed: Color = MaterialTheme.colorScheme.primary,
        foregroundColor: Color = MaterialTheme.colorScheme.onSecondary,
        foregroundColorPressed: Color = MaterialTheme.colorScheme.onPrimary
    ): PinButtonColors {
        return PinButtonColors(
            backgroundColor = backgroundColor,
            backgroundColorPressed = backgroundColorPressed,
            foregroundColor = foregroundColor,
            foregroundColorPressed = foregroundColorPressed
        )
    }

    @Composable
    fun plainPinButtonShapes(
        shape: CornerBasedShape = CircleShape
    ): PinButtonShapes {
        return PinButtonShapes(
            shape = shape,
            shapePressed = shape
        )
    }

}

@Stable
data class PinButtonColors(
    val backgroundColor: Color,
    val backgroundColorPressed: Color,
    val foregroundColor: Color,
    val foregroundColorPressed: Color
) {
    @Composable
    fun getBackgroundColor(interactionSource: InteractionSource): State<Color> {
        val animatable = remember(backgroundColor) { Animatable(backgroundColor) }
        return animatePressValue(
            animatable = animatable,
            initialValue = backgroundColor,
            targetValue = backgroundColorPressed,
            interactionSource = interactionSource
        )
    }

    @Composable
    fun getForegroundColor(interactionSource: InteractionSource): State<Color> {
        val animatable = remember(foregroundColor) { Animatable(foregroundColor) }
        return animatePressValue(
            animatable = animatable,
            initialValue = foregroundColor,
            targetValue = foregroundColorPressed,
            interactionSource = interactionSource
        )
    }
}

@Stable
data class PinButtonShapes(
    val shape: CornerBasedShape,
    val shapePressed: CornerBasedShape
) {

    @Composable
    fun getButtonShape(interactionSource: InteractionSource): State<CornerBasedShape> {
        val density = LocalDensity.current
        val size = with(density) {
            val shapeSize = PinButtonDefaults.PinButtonMinSize.toPx()
            Size(shapeSize, shapeSize)
        }

        val animatable = remember(density, size) {
            Animatable(shape, density, size)
        }
        return animatePressValue(
            animatable = animatable,
            initialValue = shape,
            targetValue = shapePressed,
            interactionSource = interactionSource
        )
    }

}

@Composable
private fun <T, V : AnimationVector> animatePressValue(
    animatable: Animatable<T, V>,
    initialValue: T,
    targetValue: T,
    interactionSource: InteractionSource
): State<T> {
    val context = LocalContext.current

    LaunchedEffect(interactionSource, initialValue, targetValue) {
        val channel = Channel<Boolean>(1, onBufferOverflow = BufferOverflow.DROP_LATEST)
        launch {
            interactionSource.interactions.collect {
                if (it is PressInteraction.Press) {
                    // Trigger vibration
                    vibrateOnClick(context)

                    if (animatable.value != targetValue) { // Fix animation deadlock
                        animatable.animateTo(
                            targetValue = targetValue,
                            animationSpec = tween(PinButtonDefaults.AnimationDurationPress),
                        )
                    }
                }

                channel.send(it is PressInteraction.Cancel || it is PressInteraction.Release)
            }
        }
        launch {
            channel.receiveAsFlow().collectLatest { shouldReset ->
                if (shouldReset) {
                    try {
                        animatable.animateTo(
                            targetValue = initialValue,
                            animationSpec = tween(PinButtonDefaults.AnimationDurationRelease)
                        )
                    } catch (e: CancellationException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    return animatable.asState()
}

@SuppressLint("NewApi")
private fun vibrateOnClick(context: Context) {
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (vibrator.hasVibrator()) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)) // 50ms vibration
    }
}
