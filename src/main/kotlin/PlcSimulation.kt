import kotlinx.coroutines.*


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
                                println("Set symbol ${element.symbol} value ${element.value}")
                                var variable = configuration.registers.getVarConfiguration(element.symbol)
                                if (variable == null) {
                                    println("ERROR: Symbol ${element.symbol} not found during Set execution")
                                    throw CancellationException("Error - Set")
                                }else {
                                    if ((variable.addressType == AddressType.COIL || variable.addressType == AddressType.DISCRETE_INPUT) && (variable.value != "0" && variable.value != "1")) {
                                        println("ERROR: Invalid value format. Symbol ${element.symbol} is of BOOL type and supports only values 0 or 1 not ${variable.value}")
                                    }
                                    when (variable.addressType) {
                                        AddressType.HOLDING_REGISTER -> TODO()
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
                                        AddressType.INPUT_REGISTER -> TODO()
                                    }
                                }
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
                                //TODO
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

}