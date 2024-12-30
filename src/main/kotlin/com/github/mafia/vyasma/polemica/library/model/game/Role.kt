package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnum
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumDeserializer
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumSerializer

@JsonSerialize(using = IntEnumSerializer::class)
@JsonDeserialize(using = RoleDeserializer::class)
enum class Role(override val value: Int) : IntEnum {
    DON(0),
    MAFIA(1),
    PEACE(2),
    SHERIFF(3)
}

class RoleDeserializer : IntEnumDeserializer<Role>(Role.entries.toTypedArray())
