package operations

import Mult
import AddressType
import Configuration
import EnvironmentVariables
import PlcMemory
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException

class MultOperation(private val configuration: Configuration,private val memory: PlcMemory, environmentVariables: EnvironmentVariables
) : BaseOperation(environmentVariables, configuration) {

    companion object {
        val logger = LoggerFactory.getLogger("MultOperation")
    }

    fun multOperation(element: Mult){
        logger.info("Mult symbol ${element.symbol} value ${element.value}")
        var value = processValue(element.value)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            logger.error("Symbol ${element.symbol} not found during Set execution")
            throw CancellationException("Error - Mult")
        } else {
            if(variable.addressType == AddressType.COIL || variable.addressType == AddressType.DISCRETE_INPUT){
                logger.error("Symbol ${element.symbol} is of type COIL or DISCRETE_INPUT which is not support by Mult operation")
                throw CancellationException("Error - Mult")
            }

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //Mult
                    //set back the new value

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if(currentValue.isEmpty()){
                            logger.error("Mult Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Mult")
                        }
                        val intValue = (( currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)
                        var floatValue = value.toFloat()
                        floatValue *= currentFloatValue
                        setHoldingRegisterFloat32(floatValue, memory, variable)
                    } else {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 1)
                        if(currentValue.isEmpty()){
                            logger.error("Mult Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Mult")
                        }
                        var intValue =  value.toInt()
                        intValue *= currentValue.first().toInt()
                        setHoldingRegisterInt16(memory, variable, intValue.toShort())
                    }
                }
                AddressType.INPUT_REGISTER -> {
                    val currentValue = memory.readInputRegister(variable.address.toInt(), 1)
                    val newValue = currentValue.first() * value.toShort()
                    memory.setInputRegister(variable.address.toInt(),newValue.toShort())
                }

                else -> {
                    throw CancellationException("Error - Mult")
                }
            }
        }
    }

}