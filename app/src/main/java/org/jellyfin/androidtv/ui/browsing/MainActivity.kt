package org.jellyfin.androidtv.ui.browsing

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import org.jellyfin.androidtv.R
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.databinding.ActivityMainBinding
import org.jellyfin.androidtv.integration.LeanbackChannelWorker
import org.jellyfin.androidtv.ui.ScreensaverViewModel
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.browsing.DestinationFragmentView
import org.jellyfin.androidtv.ui.home.HomeFragment
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationAction
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.screensaver.InAppScreensaver
import org.jellyfin.androidtv.ui.startup.StartupActivity
import org.jellyfin.androidtv.util.applyTheme
import org.jellyfin.androidtv.util.isMediaSessionKeyEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : FragmentActivity() {
	private var backPressedTime: Long = 0
	private val BACK_PRESS_DELAY = 2000 // 2 seconds

	private val navigationRepository by inject<NavigationRepository>()
	private val sessionRepository by inject<SessionRepository>()
	private val userRepository by inject<UserRepository>()
	private val screensaverViewModel by viewModel<ScreensaverViewModel>()
	private val workManager by inject<WorkManager>()

	private lateinit var binding: ActivityMainBinding

	private val backPressedCallback = object : OnBackPressedCallback(false) {
		override fun handleOnBackPressed() {
			if (navigationRepository.canGoBack) navigationRepository.goBack()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		// Apply theme before super to ensure proper theme setup
		super.onCreate(savedInstanceState)
		applyTheme()

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		if (!validateAuthentication()) return

		// Initialize background and screensaver
		binding.background.setContent { AppBackground() }
		binding.screensaver.setContent { InAppScreensaver() }

		// Set up screen keep-on
		screensaverViewModel.keepScreenOn.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
			.onEach { keepScreenOn ->
				if (keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
				else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
			}.launchIn(lifecycleScope)

		// Set up back press handling
		onBackPressedDispatcher.addCallback(this, backPressedCallback)

		// Initialize navigation
		if (savedInstanceState == null) {
			if (navigationRepository.canGoBack) {
				navigationRepository.reset(clearHistory = true)
			} else {
				// If no saved state and no back stack, navigate to home
				navigationRepository.navigate(Destinations.home, true)
			}
		} else if (!navigationRepository.canGoBack) {
			// If we're being restored but have no back stack, ensure we're at home
			navigationRepository.reset(Destinations.home, true)
		}

		// Observe navigation actions
		navigationRepository.currentAction
			.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
			.onEach { action ->
				if (action != null) {
					handleNavigationAction(action)
					backPressedCallback.isEnabled = navigationRepository.canGoBack
					screensaverViewModel.notifyInteraction(false)
				}
			}.launchIn(lifecycleScope)
	}

	override fun onResume() {
		super.onResume()

		if (!validateAuthentication()) return

		// Only apply theme if we're not in the middle of a configuration change
		if (!isChangingConfigurations) {
			applyTheme()
		}

		screensaverViewModel.activityPaused = false
	}

	private fun validateAuthentication(): Boolean {
		if (sessionRepository.currentSession.value == null || userRepository.currentUser.value == null) {
			Timber.w("Activity ${this::class.qualifiedName} started without a session, bouncing to StartupActivity")
			startActivity(Intent(this, StartupActivity::class.java))
			finish()
			return false
		}

		return true
	}

	private fun handleNavigationAction(action: NavigationAction) {
		if (isDestroyed || isFinishing) return

		try {
			screensaverViewModel.notifyInteraction(true)

			when (action) {
				is NavigationAction.NavigateFragment -> {
					if (!isDestroyed && !isFinishing) {
						Timber.d("Navigating to fragment: ${action.destination.fragment}")
						binding.contentView.navigate(action)
					}
				}
				NavigationAction.GoBack -> {
					if (!isDestroyed && !isFinishing) {
						Timber.d("Going back in fragment stack")
						binding.contentView.goBack()
					}
				}
				NavigationAction.Nothing -> {
					Timber.d("Received NavigationAction.Nothing")
				}
			}
		} catch (e: Exception) {
			Timber.e(e, "Error handling navigation action")
		}
	}

	// Forward key events to fragments
	private fun Fragment.onKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
		var result = childFragmentManager.fragments.any { it.onKeyEvent(keyCode, event) }
		if (!result && this is View.OnKeyListener) result = onKey(currentFocus, keyCode, event)
		return result
	}

	private fun onKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
		// Ignore the key event that closes the screensaver
		if (screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event?.action == KeyEvent.ACTION_UP)
			return true
		}

		return supportFragmentManager.fragments
			.any { it.onKeyEvent(keyCode, event) }
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Check if we're on the home screen by looking at the current fragment
			val currentFragment = supportFragmentManager.findFragmentById(R.id.content_view)
			val isOnHomeScreen = currentFragment is HomeFragment

			if (supportFragmentManager.backStackEntryCount > 0) {
				// Let the fragment handle the back press if there are fragments in the back stack
				return onKeyEvent(keyCode, event) || super.onKeyDown(keyCode, event)
			} else if (isOnHomeScreen) {
				// Only show exit confirmation when on the home screen
				showExitConfirmation()
				return true
			}
			// If not on home screen and no fragments in back stack, let the system handle it
			return super.onKeyDown(keyCode, event)
		}
		return onKeyEvent(keyCode, event) || super.onKeyDown(keyCode, event)
	}

	override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
		onKeyEvent(keyCode, event) || super.onKeyUp(keyCode, event)

	override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean =
		onKeyEvent(keyCode, event) || super.onKeyUp(keyCode, event)

	override fun onUserInteraction() {
		super.onUserInteraction()

		screensaverViewModel.notifyInteraction(false)
	}

	@Suppress("RestrictedApi") // False positive
	override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		// Ignore the key event that closes the screensaver
		if (!event.isMediaSessionKeyEvent() && screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event.action == KeyEvent.ACTION_UP)
			return true
		}

		@Suppress("RestrictedApi") // False positive
		return super.dispatchKeyEvent(event)
	}

	@Suppress("RestrictedApi") // False positive
	override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
		// Ignore the key event that closes the screensaver
		if (!event.isMediaSessionKeyEvent() && screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event.action == KeyEvent.ACTION_UP)
			return true
		}

		@Suppress("RestrictedApi") // False positive
		return super.dispatchKeyShortcutEvent(event)
	}

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		// Ignore the touch event that closes the screensaver
		if (screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(true)
			return true
		}

		return super.dispatchTouchEvent(ev)
	}

	private fun showExitConfirmation() {
		val dialog = AlertDialog.Builder(this, R.style.ExitDialogTheme).apply {
			setMessage(R.string.exit_app_message)
			setPositiveButton(R.string.yes) { _, _ -> finishAffinity() }
			setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
			setCancelable(true)
		}.create()

		dialog.window?.let { window ->
			// Set the background drawable
			val drawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.exit_dialog_background)
			window.setBackgroundDrawable(drawable)
			
			// Set dialog position and size
			val layoutParams = window.attributes
			val displayMetrics = resources.displayMetrics
			val screenWidth = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels)
			layoutParams.width = (screenWidth * 0.6).toInt()
			layoutParams.gravity = android.view.Gravity.CENTER
			window.attributes = layoutParams
			
			// Ensure the dialog is focusable
			window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
		}

		dialog.show()

		// Center the message text and set text color
		dialog.findViewById<android.widget.TextView>(android.R.id.message)?.apply {
			gravity = android.view.Gravity.CENTER
			setTextColor(android.graphics.Color.WHITE)
			// Adjust text size and padding
			textSize = 15f
			setPadding(8, 8, 8, 8)
		}

		try {
			// Set button text colors and focus handling
			val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
			val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
			
			positiveButton?.setTextColor(
				androidx.core.content.ContextCompat.getColor(this, R.color.theme_accent)
			)
			negativeButton?.setTextColor(
				android.graphics.Color.parseColor("#CCCCCC")
			)
			
			// Ensure buttons are focusable
			positiveButton?.isFocusable = true
			negativeButton?.isFocusable = true
			
			// Request focus on the negative button by default
			negativeButton?.requestFocus()
		} catch (e: Exception) {
			// Ignore any errors setting button colors
		}
	}
}
