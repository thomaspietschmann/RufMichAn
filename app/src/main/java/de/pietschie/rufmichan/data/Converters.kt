package de.pietschie.rufmichan.data

import androidx.room.TypeConverter
import de.pietschie.rufmichan.data.call.CallState

class Converters {
    @TypeConverter
    fun fromCallState(state: CallState): String = state.name

    @TypeConverter
    fun toCallState(value: String): CallState = CallState.valueOf(value)
}
