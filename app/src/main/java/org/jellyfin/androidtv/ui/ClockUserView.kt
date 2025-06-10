package org.jellyfin.androidtv.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.databinding.ClockUserViewBinding
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.ClockBehavior
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.jellyfin.sdk.model.api.UserDto
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ClockUserView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
	defStyleRes: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes), KoinComponent {
	private val binding: ClockUserViewBinding = ClockUserViewBinding.inflate(LayoutInflater.from(context), this, true)
	private val userPreferences by inject<UserPreferences>()
	private val userRepository by inject<UserRepository>()
	private val navigationRepository by inject<NavigationRepository>()
	private val imageHelper by inject<ImageHelper>()
	private val apiClient by inject<ApiClient>()

	var isVideoPlayer = false
		set(value) {
			field = value
			updateClockVisibility()
		}

	val homeButton get() = binding.home

	init {
		updateClockVisibility()
		
		// Load current user's profile image
		updateUserProfileImage()

		binding.home.setOnClickListener {
			navigationRepository.reset(Destinations.home, clearHistory = true)
		}

		binding.liveTv.setOnClickListener {
            // Navigate to Live TV section
            navigationRepository.reset(Destinations.liveTvGuide, clearHistory = true)
        }
	}
	
	/**
	 * Called when the view becomes visible again to refresh the user image
	 */
	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		updateUserProfileImage()
	}
	
	/**
	 * Updates the user profile image based on the current logged-in user
	 */
	private fun updateUserProfileImage() {
		val currentUser = userRepository.currentUser.value
		
		Timber.d("Updating profile image, currentUser: ${currentUser?.name}, userId: ${currentUser?.id}")
		
		// Generate the user image URL using imageApi extension
		val imageUrl = if (currentUser != null && currentUser.primaryImageTag != null) {
			apiClient.imageApi.getUserImageUrl(
				userId = currentUser.id,
				tag = currentUser.primaryImageTag,
				maxHeight = ImageHelper.MAX_PRIMARY_IMAGE_HEIGHT
			)
		} else null
		
		Timber.d("Profile image URL: $imageUrl")
		
		binding.clockUserImage.load(
			url = imageUrl,
			placeholder = ContextCompat.getDrawable(context, R.drawable.ic_user)
		)
		
		binding.clockUserImage.isVisible = currentUser != null
	}

	private fun updateClockVisibility() {
		val showClock = userPreferences[UserPreferences.clockBehavior]

		binding.clock.isVisible = when (showClock) {
			ClockBehavior.ALWAYS -> true
			ClockBehavior.NEVER -> false
			ClockBehavior.IN_VIDEO -> isVideoPlayer
			ClockBehavior.IN_MENUS -> !isVideoPlayer
		}

		binding.home.isVisible = !isVideoPlayer
	}
}
