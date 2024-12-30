package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnum
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumDeserializer
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumSerializer

@JsonSerialize(using = IntEnumSerializer::class)
@JsonDeserialize(using = PositionDeserializer::class)
enum class Position(override val value: Int) : IntEnum {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10);

    companion object {
        fun fromInt(value: Int): Position? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

class PositionDeserializer : IntEnumDeserializer<Position>(Position.entries.toTypedArray())
