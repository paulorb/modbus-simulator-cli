package operations

import Configuration
import EnvironmentVariables
import PlcMemory
import Set
import toBooleanFromBinary
import java.util.concurrent.CancellationException

class SetOperation(private val configuration: Configuration,private val memory: PlcMemory, environmentVariables: EnvironmentVariables
) : BaseOperation(environmentVariables, configuration) {
    fun setOperation(element: Set) {
        println("Set symbol ${element.symbol} value ${element.value}")
        var value = processValue(element.value)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            println("ERROR: Symbol ${element.symbol} not found during Set execution")
            throw CancellationException("Error - Set")
        } else {
            if ((variable.addressType == AddressType.COIL || variable.addressType == AddressType.DISCRETE_INPUT) && (variable.value != "0" && variable.value != "1")) {
                println("ERROR: Invalid value format. Symbol ${element.symbol} is of BOOL type and supports only values 0 or 1 not ${variable.value}")
            }
            when (variable.addressType) {
                AddressType.HOLDING_REGISTER -> {
                    if (variable.datatype == "FLOAT32") {
                        val floatValue = value.toFloat()
                        setHoldingRegisterFloat32(floatValue, memory, variable)
                    } else {
                        //Int16
                        val intValue = value.toShort()
                        setHoldingRegisterInt16(memory, variable, intValue)
                    }
                }

                AddressType.COIL -> {
                    memory.forceSingleCoil(
                        variable.address.toInt(),
                        value.toBooleanFromBinary()
                    )
                }

                AddressType.DISCRETE_INPUT -> {
                    memory.setDiscreteInput(
                        variable.address.toInt(),
                        value.toBooleanFromBinary()
                    )
                }

                AddressType.INPUT_REGISTER -> {
                    memory.setInputRegister(variable.address.toInt(), value.toShort())
                }
            }
        }
    }
}