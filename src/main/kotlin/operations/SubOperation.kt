package operations


import Configuration
import EnvironmentVariables
import IfEqual
import PlcMemory
import Sub
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException
import toBooleanFromBinary


class SubOperation(private val configuration: Configuration,private val memory: PlcMemory, environmentVariables: EnvironmentVariables
) : BaseOperation(environmentVariables, configuration) {

    companion object {
        val logger = LoggerFactory.getLogger("SubOperation")
    }
    fun subOperation(element: Sub) {
        logger.debug("Sub symbol ${element.symbol} value ${element.value}")
        var value = processValue(element.value)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            logger.error("Symbol ${element.symbol} not found during Set execution")
            throw CancellationException("Error - Sub")
        } else {
            if (variable.addressType == AddressType.COIL || variable.addressType == AddressType.DISCRETE_INPUT) {
                logger.error("Symbol ${element.symbol} is of type COIL or DISCRETE_INPUT which is not support by Sub operation")
                throw CancellationException("Error - Sub")
            }

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //add
                    //set back the new value

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if (currentValue.isEmpty()) {
                            logger.error("Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        val intValue = ((currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)
                        var floatValue = value.toFloat()
                        floatValue -= currentFloatValue
                        setHoldingRegisterFloat32(floatValue, memory, variable)
                    } else {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 1)
                        if (currentValue.isEmpty()) {
                            logger.error("Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        var intValue = value.toInt()
                        intValue -= currentValue.first().toInt()
                        setHoldingRegisterInt16(memory, variable, intValue.toShort())
                    }
                }

                AddressType.INPUT_REGISTER -> {
                    val currentValue = memory.readInputRegister(variable.address.toInt(), 1)
                    val newValue = currentValue.first() - value.toShort()
                    memory.setInputRegister(variable.address.toInt(), newValue.toShort())
                }

                else -> {
                    throw CancellationException("Error - Sub")
                }
            }
        }
    }
}