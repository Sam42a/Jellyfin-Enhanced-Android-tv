package org.jellyfin.androidtv.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.data.repository.NotificationsRepository
import org.jellyfin.androidtv.databinding.FragmentHomeBinding
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.playback.PlaybackLauncher
import org.jellyfin.androidtv.ui.startup.StartupActivity
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.liveTvApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

// Removed interface as we'll use direct field access

class HomeFragment : Fragment() {
    private val api: ApiClient by inject()
    private val imageHelper: ImageHelper by inject()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    @JvmField
    var isReadyForInteraction = false

    private val sessionRepository by inject<SessionRepository>()
    private val userRepository by inject<UserRepository>()
    private val serverRepository by inject<ServerRepository>()
    private val notificationRepository by inject<NotificationsRepository>()
    private val navigationRepository by inject<NavigationRepository>()
    private val mediaManager by inject<MediaManager>()
    private val playbackLauncher: PlaybackLauncher by inject()
    private val userSettingPreferences: UserSettingPreferences by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val backgroundService: org.jellyfin.androidtv.data.service.BackgroundService by inject()

    override fun onResume() {
        super.onResume()
        // Clear backdrop when navigating to home
        try {
            backgroundService.clearBackgrounds()
        } catch (e: Exception) {
            // Ignore any errors when clearing backdrop
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Removed onAttach and onDetach as they're no longer needed with direct field access

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initially block interactions
        view.isFocusableInTouchMode = false
        view.isClickable = false
        
        // Set a delay to enable interactions after initial load
        view.postDelayed({
            isReadyForInteraction = true
            view.isFocusableInTouchMode = true
            view.isClickable = true
        }, 1000) // 1 second delay, adjust based on your needs

        binding.toolbar.setContent {
            val searchAction = {
                // Navigate to search screen
                navigationRepository.navigate(Destinations.search())
            }
            val settingsAction = {
                // Open preferences/settings activity
                val intent = Intent(requireContext(), org.jellyfin.androidtv.ui.preference.PreferencesActivity::class.java)
                startActivity(intent)
            }
            val switchUsersAction = {
                switchUser()
            }

            val liveTvAction = {
    val lastChannelId = org.jellyfin.androidtv.ui.livetv.TvManager.getLastLiveTvChannel()
    if (lastChannelId != null) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val channel = withContext(Dispatchers.IO) {
                    api.liveTvApi.getChannel(lastChannelId).content
                }
                // Launch playback for the channel
                playbackLauncher.launch(requireContext(), listOf(channel), 0, false, 0, false)
            } catch (e: Exception) {
                // If fetch fails, fallback to guide
                navigationRepository.navigate(Destinations.liveTvGuide)
            }
        }
    } else {
        navigationRepository.navigate(Destinations.liveTvGuide)
    }
}
            val libraryAction = {
                // Navigate to the home screen which shows the library content
                navigationRepository.navigate(Destinations.home)
            }
            org.jellyfin.androidtv.ui.shared.toolbar.HomeToolbar(
                openSearch = { searchAction() },
                openLiveTv = { liveTvAction() },
                openSettings = { settingsAction() },
                switchUsers = { switchUsersAction() },
                openLibrary = { libraryAction() },
                userSettingPreferences = userSettingPreferences
            )
        }
    }

    private fun switchUser() {
        if (!isReadyForInteraction) return
        
        mediaManager.clearAudioQueue()
        sessionRepository.destroyCurrentSession()

        val selectUserIntent = Intent(activity, StartupActivity::class.java)
        selectUserIntent.putExtra(StartupActivity.EXTRA_HIDE_SPLASH, true)
        selectUserIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        activity?.startActivity(selectUserIntent)
        activity?.finishAfterTransition()
    }

    private fun openItemDetails(item: org.jellyfin.sdk.model.api.BaseItemDto) {
        item.id?.let { idStr ->
            val uuid = try {
                UUID.fromString(idStr.toString())
            } catch (e: Exception) {
                null
            }
            if (uuid != null) {
                navigationRepository.navigate(Destinations.itemDetails(uuid)) // itemDetails expects UUID

            }
        }
    }
}
