package operations

import Configuration
import EnvironmentVariables
import PlcMemory
import Toggle
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException

class ToggleOperation(private val configuration: Configuration,private val memory: PlcMemory, environmentVariables: EnvironmentVariables
) : BaseOperation(environmentVariables, configuration, memory)
{
    companion object {
        val logger = LoggerFactory.getLogger("ToggleOperation")
    }

    fun toggleOperation(element: Toggle) {
        SubOperation.logger.info("Toggle symbol ${element.symbol}")
        var value = processValue(element.symbol)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            SubOperation.logger.error("Symbol ${element.symbol} not found during Set execution")
            throw CancellationException("Error - Sub")
        } else {
            if (variable.addressType != AddressType.COIL) {
                SubOperation.logger.error("Symbol ${element.symbol} is not of type COIL, only COIL is supported by Toggle operation")
                throw CancellationException("Error - Toggle")
            }

            when (variable.addressType) {
                AddressType.COIL -> {
                    if (variable.datatype == "BOOL" || variable.datatype == "") {
                        val currentValue = memory.readCoilStatus(variable.address.toInt(), 1)
                        memory.forceSingleCoil(variable.address.toInt(), currentValue.first().not())
                    } else {
                        throw CancellationException("Error - Toggle - Invalid datatype")
                    }
                }

                else -> {
                    throw CancellationException("Error - Toggle")
                }
            }
        }
    }
}