package com.example.circularplanner.ui.component

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.ripple
import com.example.circularplanner.ui.viewmodel.DataViewModel

enum class ListDirection {
    START,
    END
}

@Composable
fun Calendar(
    viewModel: DataViewModel
    ) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedDateIndex by remember { mutableStateOf(0) }
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("d")
    var numberOfDaysPerWeek = 7
    val daysOfWeek = mutableListOf<String>()
    for (dayOfWeek in DayOfWeek.entries) {
        val localizedDayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        daysOfWeek += localizedDayName
//        daysOfWeek += simpleDateFormat.format(localizedDayName)
    }

    val listState = rememberLazyListState()
    val flingBehaviour = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center
    )
    val reachedListEnd by remember {
        derivedStateOf {
            // Check if the visible item is the last item in the list
            val visibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            visibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }
    }
    val reachedListStart by remember {
        derivedStateOf {
            // Check if the visible item is the first item in the list
            val visibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            visibleItem?.index == 0
        }
    }

    fun getWeek (date: LocalDate = LocalDate.now()) : List<LocalDate> {
        // Gives the first day of the week (Monday) in which the given day is
        val firstDayOfTheWeek = date.with(DayOfWeek.MONDAY)
        var day = firstDayOfTheWeek
        var week = List<LocalDate>(
            size = 7,
            init = { index ->
                day.plusDays(index.toLong())
            }
        )

        return week
    }

    fun getNextWeek (date: LocalDate = LocalDate.now()) : List<LocalDate> {
        return getWeek(date.plusWeeks(1))
    }

    fun getPreviousWeek (date: LocalDate = LocalDate.now()) : List<LocalDate> {
        return getWeek(date.minusWeeks(1))
    }

    var weeks by remember { mutableStateOf(listOf(getWeek())) }

    fun loadMore (direction: ListDirection) : Unit {
        // Append more data to the beginning of the list
        if (direction == ListDirection.START) {
            val week = getPreviousWeek(weeks[0][0])
            var newWeeks = List(
                size = (weeks.size + 1),
                init = { index ->
                    if (index == 0) {
                        week
                    } else {
                        weeks[index - 1]
                    }
                }
            )
            weeks = newWeeks
        }
        // Append more data to the end of the list
        else if (direction == ListDirection.END) {
            val week = getNextWeek(weeks[weeks.size - 1][0])
            var newWeeks = List(
                size = (weeks.size + 1),
                init = { index ->
                    if (index == weeks.size) {
                        week
                    } else {
                        weeks[index]
                    }
                }
            )
            weeks = newWeeks
        }
    }

    fun setInitialSelectedDateIndex () {
        weeks[0].forEachIndexed { index, day ->
            if (day == today) {
                selectedDateIndex = index
            }
        }
    }

    fun setSelectedDateOnScroll () {
        val visibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
        if (visibleItem != null) {
            val date = weeks[visibleItem.index][selectedDateIndex]
            selectedDate = date
            viewModel.setDate(date)
        }
    }

    fun setSelectedDateOnClick (index: Int, date: LocalDate) {
        selectedDateIndex = index
        selectedDate = date
        viewModel.setDate(date)
    }

    setInitialSelectedDateIndex()

    LaunchedEffect(key1 = listState.firstVisibleItemIndex) {
        setSelectedDateOnScroll()
    }

    // Load more if scrolled to end or start of the list
    LaunchedEffect(key1 = reachedListStart, key2 = reachedListEnd) {
        if (reachedListStart) loadMore(ListDirection.START)
        if (reachedListEnd) loadMore(ListDirection.END)
    }

    Column () {
        // Weekday names
        Row (
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(numberOfDaysPerWeek) { iteration ->
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(5.dp)
                ) {
                    Text (
                        text = daysOfWeek[iteration],
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }

        // Month days
        LazyRow (
            modifier = Modifier
                .fillMaxWidth(),
            state = listState,
            flingBehavior = flingBehaviour
        ) {
            items(items = weeks, key = { it }) { week ->
                Row (
                    modifier = Modifier
                        .fillParentMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(numberOfDaysPerWeek) { iteration ->
                        val todayModifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)

                        val selectedDayModifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = CircleShape
                            )

                        Box (
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(5.dp)
                                .conditional(week[iteration] == today, todayModifier)
                                .conditional(iteration == selectedDateIndex, selectedDayModifier)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = {
                                        setSelectedDateOnClick(
                                            index = iteration,
                                            date = week[iteration]
                                        )
                                    }
                                )
                        ) {
                            Text (
                                text = week[iteration].format(formatter),
                                modifier = Modifier
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.conditional (condition: Boolean, modifier: Modifier) : Modifier {
    return if (condition) {
        then(modifier)
    } else {
        return this
    }
}