package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnum
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumDeserializer
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumSerializer

@JsonSerialize(using = IntEnumSerializer::class)
@JsonDeserialize(using = PolemicaGameResultDeserializer::class)
enum class PolemicaGameResult(
    override val value: Int
) : IntEnum {
    RED_WIN(0),
    BLACK_WIN(1)
}

class PolemicaGameResultDeserializer :
    IntEnumDeserializer<PolemicaGameResult>(PolemicaGameResult.entries.toTypedArray())
