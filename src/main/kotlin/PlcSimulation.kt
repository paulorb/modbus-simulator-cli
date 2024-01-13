import kotlinx.coroutines.*
import java.lang.Float


class PlcSimulation(
    configurationParser: ConfigurationParser,
    memory: PlcMemory,
    coroutineScope: CoroutineScope
) {

    init {
        var simulationConfiguration = configurationParser.getConfiguredDevice().simulation
        var configuration = configurationParser.getConfiguredDevice().configuration
        coroutineScope.async {
            try {
                while (this.isActive) {
                    val startTime = System.currentTimeMillis()
                    simulationConfiguration.randomElements.forEach { element ->
                        when (element) {
                            is Set -> {
                                setOperation(element, configuration, memory)
                            }

                            is Random -> {
                                println("Random symbol ${element.symbol} valueMax ${element.valueMax} valueMin ${element.valueMin}")
                            }

                            is Delay -> {
                                println("Delay value ${element.value}")
                                delay(element.value.toLong())
                            }

                            is Linear -> {
                                //TODO
                            }

                            is Add -> {
                                addOperation(element, configuration, memory)
                            }

                            is Sub -> {
                                //TODO
                            }

                            else -> throw UnsupportedOperationException("Unknown simulation step type")
                        }
                    }
                    val endTime = System.currentTimeMillis()
                    // Calculate the time difference
                    val elapsedTime = endTime - startTime
                    if (elapsedTime < simulationConfiguration.plcScanTime) {
                        delay(simulationConfiguration.plcScanTime - elapsedTime)
                    }
                }
            } catch (e: CancellationException) {
                println("Job: Caught CancellationException - ${e.message}")
             } finally {
                 println("Job: Finally block, cleaning up resources")
            }
        }

    }

    private fun addOperation(element: Add, configuration: Configuration, memory: PlcMemory){
        println("Add symbol ${element.symbol} value ${element.value}")
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            println("ERROR: Symbol ${element.symbol} not found during Set execution")
            throw CancellationException("Error - Add")
        } else {
            if(variable.addressType == AddressType.COIL || variable.addressType == AddressType.DISCRETE_INPUT){
                println("ERROR: Symbol ${element.symbol} is of type COIL or DISCRETE_INPUT which is not support by Add operation")
                throw CancellationException("Error - Add")
            }

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //add
                    //set back the new value

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if(currentValue.isEmpty()){
                            println("ERROR: Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        val intValue = (( currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)
                        var floatValue = element.value.toFloat()
                        floatValue += currentFloatValue
                        setHoldingRegisterFloat32(floatValue, memory, variable)
                    } else {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 1)
                        if(currentValue.isEmpty()){
                            println("ERROR: Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        var intValue =  element.value.toInt()
                        intValue += currentValue.first().toInt()
                        setHoldingRegisterInt16(memory, variable, intValue.toShort())
                    }
                }
                AddressType.INPUT_REGISTER -> {
                    val currentValue = memory.readInputRegister(variable.address.toInt(), 1)
                    val newValue = currentValue.first() + element.value.toShort()
                    memory.setInputRegister(variable.address.toInt(),newValue.toShort())
                }

                else -> {
                    throw CancellationException("Error - Add")
                }
            }
        }
    }

    private fun setOperation(element: Set, configuration: Configuration, memory: PlcMemory) {
        println("Set symbol ${element.symbol} value ${element.value}")
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
                        val floatValue = element.value.toFloat()
                        setHoldingRegisterFloat32(floatValue, memory, variable)
                    } else {
                        //Int16
                        val intValue =  element.value.toShort()
                        setHoldingRegisterInt16(memory, variable, intValue)
                    }
                }

                AddressType.COIL -> {
                    memory.forceSingleCoil(
                        variable.address.toInt(),
                        element.value.toBooleanFromBinary()
                    )
                }

                AddressType.DISCRETE_INPUT -> {
                    memory.setDiscreteInput(
                        variable.address.toInt(),
                        element.value.toBooleanFromBinary()
                    )
                }

                AddressType.INPUT_REGISTER -> {
                    memory.setInputRegister(variable.address.toInt(), element.value.toShort())
                }
            }
        }
    }

    private fun setHoldingRegisterInt16(memory: PlcMemory, variable: Register, intValue: Short) {
        memory.presetMultipleRegisters(
            mutableListOf<Pair<Int, Short>>(
                Pair<Int, Short>(
                    variable.address.toInt(),
                    intValue
                )
            )
        )
    }

    private fun setHoldingRegisterFloat32(floatValue: kotlin.Float, memory: PlcMemory, variable: Register) {
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

}