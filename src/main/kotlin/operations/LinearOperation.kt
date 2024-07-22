package operations

import Configuration
import PlcMemory
import Linear
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException

class LinearOperation {
    private var linearVariables: MutableMap<String, Double> = mutableMapOf<String, Double>()

    companion object {
        val logger = LoggerFactory.getLogger("LinearOperation")
    }

    private fun getNextValue(linear: Linear): String {
        var x =0.0
        if(linear.endX > linear.startX) {
            //crescente
            x = linearVariables.getOrDefault(linear.symbol, linear.startX)
            if (x + linear.step <= linear.endX) {
                linearVariables[linear.symbol] = x + linear.step
            } else {
                if (linear.replay) {
                    linearVariables[linear.symbol] = linear.startX
                }
            }
        }else{
            //decrescente
            x = linearVariables.getOrDefault(linear.symbol, linear.startX)
            if (x - linear.step >= linear.endX) {
                linearVariables[linear.symbol] = x - linear.step
            } else {
                if (linear.replay) {
                    linearVariables[linear.symbol] = linear.startX
                }
            }
        }
        return (linear.a * x + linear.b).toString()
    }

    fun process(element: Linear, configuration: Configuration, memory: PlcMemory) {
        var nextValue = getNextValue(element)
        logger.info("Linear symbol ${element.symbol} value $nextValue")
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            logger.error("Symbol ${element.symbol} not found during Linear execution")
            throw CancellationException("Error - Linear")
        } else {
            if (variable.addressType == AddressType.COIL || variable.addressType == AddressType.DISCRETE_INPUT) {
                logger.error("Symbol ${element.symbol} is of type COIL or DISCRETE_INPUT which is not support by Linear operation")
                throw CancellationException("Error - Linear")
            }

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //add
                    //set back the new value

                    if (variable.datatype == "FLOAT32") {

                        setHoldingRegisterFloat32(nextValue.toFloat(), memory, variable)
                    } else {

                        setHoldingRegisterInt16(memory, variable, nextValue.toFloat().toInt().toShort())
                    }
                }

                AddressType.INPUT_REGISTER -> {
                    memory.setInputRegister(variable.address.toInt(), nextValue.toFloat().toInt().toShort())
                }

                else -> {
                    throw CancellationException("Error - Linear")
                }
            }
        }
    }
}