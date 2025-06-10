package org.jellyfin.androidtv.preference.constant

import org.jellyfin.androidtv.R
import org.jellyfin.preference.PreferenceEnum

enum class AppTheme(
	override val nameRes: Int,
) : PreferenceEnum {
	/**
	 * The purple haze theme: deep blue and purple gradient
	 */
	PURPLE_HAZE(R.string.pref_theme_purple_haze),
	/**
	 * The default dark theme
	 */
	DARK(R.string.pref_theme_dark),

	/**
	 * The "classic" emerald theme
	 */
	EMERALD(R.string.pref_theme_emerald),

	/**
	 * A theme with a more muted accent color, inspired by CTalvio's Monochromic CSS theme for Jellyfin Web
	 */
	MUTED_PURPLE(R.string.pref_theme_muted_purple),

	/**
	 * A minimal theme optimized for low-end devices with basic colors and reduced animations
	 */
	BASIC(R.string.pref_theme_basic)
}
