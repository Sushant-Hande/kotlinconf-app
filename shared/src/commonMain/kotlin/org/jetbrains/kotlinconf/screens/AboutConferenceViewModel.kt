package org.jetbrains.kotlinconf.screens

import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.kotlinconf.ConferenceService
import org.jetbrains.kotlinconf.SessionCardView
import org.jetbrains.kotlinconf.Speaker

data class KeyEvent(
    val title: String,
    val month: String,
    val day: String,
    val dayTo: String?,
    val description: String,
    val speakers: List<Speaker>,
    val locationLine: String,
    val timeLine: String,
)

private fun SessionCardView.toKeyEvent(service: ConferenceService) = KeyEvent(
    title = this.title,
    month = this.startsAt.month.name.uppercase(),
    day = this.startsAt.dayOfMonth.toString(),
    dayTo = null,
    description = this.description,
    speakers = this.speakerIds.mapNotNull {
        service.speakerById(it)
    },
    locationLine = locationLine,
    timeLine = timeLine,
)

class AboutConferenceViewModel(
    service: ConferenceService,
) : ViewModel() {
    val events: StateFlow<List<KeyEvent>> = service.sessionCards.map {
        listOfNotNull(
            it.firstOrNull { it.title == "Opening keynote" }?.toKeyEvent(service),
            it.firstOrNull { it.title == "Party" }?.toKeyEvent(service),
            it.firstOrNull { it.title == "Second day keynote" }?.toKeyEvent(service),
            it.firstOrNull { it.title == "Closing Panel" }?.toKeyEvent(service),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firstDayKeynote: StateFlow<KeyEvent?> = service.sessionCards.map { it.firstOrNull { it.title == "Opening keynote" }?.toKeyEvent(service) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}