package operations

import Configuration
import EnvironmentVariables
import PlcMemory
import Trace
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException

class TraceOperation(
    private val configuration: Configuration, private val memory: PlcMemory, environmentVariables: EnvironmentVariables

) : BaseOperation(environmentVariables, configuration) {
    companion object {
        val logger = LoggerFactory.getLogger("TraceOperation")
    }

    fun traceOperation(element: Trace) {
        var valueToTrace : String = ""
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            TraceOperation.logger.error("Symbol ${element.symbol} not found during Trace execution")
            throw CancellationException("Error - Trace")
        } else {
            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if (currentValue.isEmpty()) {
                            SubOperation.logger.error("Trace Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Trace")
                        }
                        val intValue = ((currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)
                        valueToTrace = currentFloatValue.toString()
                    } else {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 1)
                        if (currentValue.isEmpty()) {
                            SubOperation.logger.error("Trace Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        valueToTrace =  currentValue.first().toInt().toString()
                    }
                }

                AddressType.INPUT_REGISTER -> {
                    val currentValue = memory.readInputRegister(variable.address.toInt(), 1)
                    valueToTrace = currentValue.first().toString()
                }

                AddressType.COIL -> {
                    val currentValue = memory.readCoilStatus(variable.address.toInt(), 1)
                    valueToTrace = currentValue.first().toString()
                }

                AddressType.DISCRETE_INPUT -> {
                    val currentValue = memory.readInputStatus(variable.address.toInt(), 1)
                    valueToTrace = currentValue.first().toString()
                }

            }
            TraceOperation.logger.info("TRACE - Symbol: ${element.symbol} Value: $valueToTrace")

        }
    }

}