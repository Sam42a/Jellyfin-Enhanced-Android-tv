package org.jellyfin.androidtv.ui.playback.overlay

import android.content.Context
import android.util.AttributeSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.playback.segment.MediaSegmentRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SkipOverlayComposable(
	visible: Boolean,
) {
	Box(
		contentAlignment = Alignment.BottomEnd,
		modifier = Modifier
			.padding(48.dp, 48.dp)
	) {
		AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(16.dp))
					.background(colorResource(android.R.color.black).copy(alpha = 0.7f))
					.padding(horizontal = 16.dp, vertical = 8.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Text(
						text = stringResource(R.string.segment_action_skip).uppercase(),
						color = colorResource(android.R.color.white),
						fontSize = 16.sp
					)
					Image(
						painter = painterResource(R.drawable.ic_fast_forward),
						contentDescription = null,
						colorFilter = ColorFilter.tint(colorResource(android.R.color.white)),
						modifier = Modifier.size(20.dp)
					)
				}
			}
		}
	}
}

class SkipOverlayView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {
	private val _currentPosition = MutableStateFlow(Duration.ZERO)
	private val _targetPosition = MutableStateFlow<Duration?>(null)
	private val _skipUiEnabled = MutableStateFlow(true)

	var currentPosition: Duration
		get() = _currentPosition.value
		set(value) {
			_currentPosition.value = value
		}

	var currentPositionMs: Long
		get() = _currentPosition.value.inWholeMilliseconds
		set(value) {
			_currentPosition.value = value.milliseconds
		}

	var targetPosition: Duration?
		get() = _targetPosition.value
		set(value) {
			_targetPosition.value = value
		}

	var targetPositionMs: Long?
		get() = _targetPosition.value?.inWholeMilliseconds
		set(value) {
			_targetPosition.value = value?.milliseconds
		}

	var skipUiEnabled: Boolean
		get() = _skipUiEnabled.value
		set(value) {
			_skipUiEnabled.value = value
		}

	val visible: Boolean
		get() {
			val enabled = _skipUiEnabled.value
			val targetPosition = _targetPosition.value
			val currentPosition = _currentPosition.value

			return enabled && targetPosition != null && currentPosition <= (targetPosition - MediaSegmentRepository.SkipMinDuration)
		}

	@Composable
	override fun Content() {
		val skipUiEnabled by _skipUiEnabled.collectAsState()
		val currentPosition by _currentPosition.collectAsState()
		val targetPosition by _targetPosition.collectAsState()

		val visible by remember(skipUiEnabled, currentPosition, targetPosition) {
			derivedStateOf { visible }
		}

		// Auto hide
		LaunchedEffect(skipUiEnabled, targetPosition) {
			delay(MediaSegmentRepository.AskToSkipAutoHideDuration)
			_targetPosition.value = null
		}

		SkipOverlayComposable(visible)
	}
}
