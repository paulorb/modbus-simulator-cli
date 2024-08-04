package operations

import PlcMemory
import Register
import java.lang.Float
import kotlin.Int
import kotlin.Pair
import kotlin.Short

fun setHoldingRegisterFloat32(floatValue: kotlin.Float, memory: PlcMemory, variable: Register) {
    val intValue = Float.floatToIntBits(floatValue)
    val lowWord = intValue and 0xFFFF
    val highWord = (intValue ushr 16) and 0xFFFF
    memory.presetMultipleRegisters(
        mutableListOf<Pair<Int, Short>>(
            Pair<Int, Short>(
                variable.address.toInt(),
                lowWord.toShort()
            ),
            Pair<Int, Short>(
                variable.address.toInt() + 1,
                highWord.toShort()
            ),
        )
    )
}

fun setHoldingRegisterInt16(memory: PlcMemory, variable: Register, intValue: Short) {
    memory.presetMultipleRegisters(
        mutableListOf<Pair<Int, Short>>(
            Pair<Int, Short>(
                variable.address.toInt(),
                intValue
            )
        )
    )
}