package org.jellyfin.androidtv.preference

import android.content.Context
import androidx.preference.PreferenceManager
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.preference.enumPreference
import org.jellyfin.preference.intPreference
import org.jellyfin.preference.booleanPreference
import org.jellyfin.preference.store.SharedPreferenceStore

class UserSettingPreferences(context: Context) : SharedPreferenceStore(
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
) {
	    val showComedyRow = booleanPreference("showComedyRow", true)
    val showRomanceRow = booleanPreference("showRomanceRow", true)
    val showAnimeRow = booleanPreference("showAnimeRow", true)
    val showAnimationRow = booleanPreference("showAnimationRow", false)
    val showActionRow = booleanPreference("showActionRow", true)
    val showActionAdventureRow = booleanPreference("showActionAdventureRow", true)
    val showFavoritesRow = booleanPreference("showFavoritesRow", true)
    val showMyCollectionsRow = booleanPreference("showMyCollectionsRow", true)
    val showSciFiRow = booleanPreference("showSciFiRow", true)
    val showDocumentaryRow = booleanPreference("showDocumentaryRow", true)
    val showDramaRow = booleanPreference("showDramaRow", false)
    val showRealityTvRow = booleanPreference("showRealityTvRow", false)
    val showFamilyRow = booleanPreference("showFamilyRow", false)
    val showHorrorRow = booleanPreference("showHorrorRow", true)
    val showFantasyRow = booleanPreference("showFantasyRow", true)
    val showHistoryRow = booleanPreference("showHistoryRow", false)
    val showMusicRow = booleanPreference("showMusicRow", false)
    val showMysteryRow = booleanPreference("showMysteryRow", true)
    val showRealityRow = booleanPreference("showRealityRow", false)
    val showThrillerRow = booleanPreference("showThrillerRow", true)
    val showWarRow = booleanPreference("showWarRow", false)

    private val defaultGenreOrder = listOf(
        "Comedy",
        "Romance",
        "Anime",
        "Animation",
        "Action",
        "Sci-Fi",
        "Documentary",
        "Drama",
        "Family",
        "Horror",
        "Fantasy",
        "History",
        "Music",
        "Mystery",
        "Reality",
        "Thriller",
        "War"
    )
    private val genreRowOrderKey = "genreRowOrder"

    @JvmField
    val skipBackLength = intPreference("skipBackLength", 10_000)
    @JvmField
    val skipForwardLength = intPreference("skipForwardLength", 30_000)

    // Media folder display options
    val useExtraSmallMediaFolders = booleanPreference("useExtraSmallMediaFolders", true)
    val showLiveTvButton = booleanPreference("show_live_tv_button", false)

    val homesection0 = enumPreference("homesection0", HomeSectionType.LIBRARY_TILES_SMALL)
    val homesection1 = enumPreference("homesection1", HomeSectionType.RESUME)
    val homesection2 = enumPreference("homesection2", HomeSectionType.NONE)
    val homesection3 = enumPreference("homesection3", HomeSectionType.NONE)
    val homesection4 = enumPreference("homesection4", HomeSectionType.NONE)
    val homesection5 = enumPreference("homesection5", HomeSectionType.NEXT_UP)
    val homesection6 = enumPreference("homesection6", HomeSectionType.LATEST_MEDIA)
    val homesection7 = enumPreference("homesection7", HomeSectionType.NONE)
    val homesection8 = enumPreference("homesection8", HomeSectionType.NONE)
    val homesection9 = enumPreference("homesection9", HomeSectionType.NONE)

    fun getGenreRowOrder(): List<String> {
        val raw = getString(genreRowOrderKey, defaultGenreOrder.joinToString(","))
        return raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    }
    fun setGenreRowOrder(order: List<String>) {
        setString(genreRowOrderKey, order.joinToString(","))
    }
}
