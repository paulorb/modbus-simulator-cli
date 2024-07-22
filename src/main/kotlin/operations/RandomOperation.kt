package operations

import AddressType
import Configuration
import PlcMemory
import Random
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException


fun randomOperation(element: Random, configuration: Configuration, memory: PlcMemory){
    val logger = LoggerFactory.getLogger("randomOperation")
    logger.info("Random symbol ${element.symbol} value min ${element.valueMin} value max ${element.valueMax}")
    var variable = configuration.registers.getVarConfiguration(element.symbol)
    if (variable == null) {
        logger.error("Symbol ${element.symbol} not found during Set execution")
        throw CancellationException("Error - Random")
    } else {
        if(variable.addressType == AddressType.COIL || variable.addressType == AddressType.DISCRETE_INPUT){
            logger.error("Symbol ${element.symbol} is of type COIL or DISCRETE_INPUT which is not support by Random operation")
            throw CancellationException("Error - Random")
        }

        when (variable.addressType) {

            AddressType.HOLDING_REGISTER -> {
                //get the current value
                //add
                //set back the new value

                if (variable.datatype == "FLOAT32") {
                    val random: Float = element.valueMin.toFloat() + kotlin.random.Random.nextFloat() * (element.valueMax.toFloat() - element.valueMin.toFloat())
                    setHoldingRegisterFloat32(random, memory, variable)
                } else {
                    val random: Int = element.valueMin.toInt() + kotlin.random.Random.nextInt() * (element.valueMax.toInt() - element.valueMin.toInt())
                    setHoldingRegisterInt16(memory, variable, random.toShort())
                }
            }
            AddressType.INPUT_REGISTER -> {
                val random: Int = element.valueMin.toInt() + kotlin.random.Random.nextInt() * (element.valueMax.toInt() - element.valueMin.toInt())
                memory.setInputRegister(variable.address.toInt(),random.toShort())
            }

            else -> {
                throw CancellationException("Error - Random")
            }
        }
    }
}