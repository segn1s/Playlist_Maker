package org.segn1s.playlistmaker.presentation.media.favs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.segn1s.playlistmaker.domain.api.favorites.FavoriteTracksInteractor
import org.segn1s.playlistmaker.domain.model.Track

sealed interface FavoriteTracksState {
    object Empty : FavoriteTracksState
    data class Content(val tracks: List<Track>) : FavoriteTracksState
}

class FavoriteTracksViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val _state = MutableLiveData<FavoriteTracksState>(FavoriteTracksState.Empty)
    val state: LiveData<FavoriteTracksState> = _state

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteTracksInteractor.getFavoriteTracks()
                .collectLatest { tracks ->
                    if (tracks.isEmpty()) {
                        _state.postValue(FavoriteTracksState.Empty)
                    } else {
                        _state.postValue(FavoriteTracksState.Content(tracks))
                    }
                }
        }
    }
}
