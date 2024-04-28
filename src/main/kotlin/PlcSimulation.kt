import kotlinx.coroutines.*
import operations.*
import java.lang.Float
import java.util.concurrent.CancellationException

class PlcSimulation(
    configurationParser: ConfigurationParser,
    memory: PlcMemory,
    coroutineScope: CoroutineScope
) {
    val linearOperations = LinearOperation()
    val csvOperations = CsvOperation()
    init {
        var simulationConfiguration = configurationParser.getConfiguredDevice().simulation
        var configuration = configurationParser.getConfiguredDevice().configuration
        coroutineScope.async {
            try {
                while (this.isActive) {
                    val startTime = System.currentTimeMillis()
                    simulationConfiguration.randomElements.forEach { element ->
                        processOperationElement(element, configuration, memory)
                    }
                    val endTime = System.currentTimeMillis()
                    // Calculate the time difference
                    val elapsedTime = endTime - startTime
                    if (elapsedTime < simulationConfiguration.plcScanTime) {
                        delay(simulationConfiguration.plcScanTime - elapsedTime)
                    }
                }
            } catch (e: Exception) {
                println("Exception - ${e.message}")
             } finally {
                 println("Job: Finally block, cleaning up resources")
            }
        }

    }

    suspend fun processOperationElement(element: Any, configuration: Configuration, memory: PlcMemory) {
        when (element) {
            is Set -> {
                setOperation(element, configuration, memory)
            }

            is Random -> {
                randomOperation(element, configuration, memory)
            }

            is Delay -> {
                println("Delay value ${element.value}")
                delay(element.value.toLong())
            }

            is Linear -> {
                linearOperations.process(element, configuration, memory)
            }

            is Add -> {
                addOperation(element, configuration, memory)
            }

            is Sub -> {
                subOperation(element, configuration, memory)
            }

            is Csv -> {
                csvOperations.process(element, configuration, memory)
            }

            is IfEqual -> {
                ifEqual(element, configuration, memory)
            }

            else -> throw UnsupportedOperationException("Unknown simulation step type")
        }
    }



    suspend fun ifEqual(element: IfEqual, configuration: Configuration, memory: PlcMemory) {
        println("IfEqual symbol ${element.symbol} value ${element.value}")
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            println("ERROR: Symbol ${element.symbol} not found during IfEqual execution")
            throw CancellationException("Error - IfEqual")
        } else {

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //compare
                    //execute operations

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if(currentValue.isEmpty()){
                            println("ERROR: Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        val intValue = (( currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)

                        //compare
                        if(currentFloatValue != element.value.toFloat()){
                            //abort and continue
                            //since the value does not match the condition
                            return
                        }
                        //execute operations
                        element.randomElements.forEach { subElement ->
                            processOperationElement(subElement, configuration, memory)
                        }


                    } else {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 1)
                        if(currentValue.isEmpty()){
                            println("ERROR: IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - IfEqual")
                        }
                        //compare
                        if(currentValue.first().toInt() != element.value.toInt()){
                            //abort and continue
                            //since the value does not match the condition
                            return
                        }
                        //execute operations
                        element.randomElements.forEach { subElement ->
                            processOperationElement(subElement, configuration, memory)
                        }

                    }
                }
                AddressType.INPUT_REGISTER -> {
                    val currentValue = memory.readInputRegister(variable.address.toInt(), 1)
                    if(currentValue.isEmpty()){
                        println("ERROR: IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                        throw CancellationException("Error - IfEqual")
                    }
                    //compare
                    if( currentValue.first().toShort() != element.value.toShort()){
                        //abort and continue
                        //since the value does not match the condition
                        return
                    }
                    //execute operations
                    element.randomElements.forEach { subElement ->
                        processOperationElement(subElement, configuration, memory)
                    }
                }
                AddressType.COIL -> {
                    if ( (element.value != "0" && element.value != "1")) {
                        println("ERROR: Invalid value format on IfEqual. Symbol ${element.symbol} is of BOOL type and supports only values 0 or 1 not ${element.value} for comparison")
                    }
                    var currentValue = memory.readCoilStatus(variable.address.toInt(), 2)
                    if(currentValue.isEmpty()){
                        println("ERROR: IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                        throw CancellationException("Error - IfEqual")
                    }
                    //compare
                    if( currentValue.first() != element.value.toBoolean()){
                        //abort and continue
                        //since the value does not match the condition
                        return
                    }
                    //execute operations
                    element.randomElements.forEach { subElement ->
                        processOperationElement(subElement, configuration, memory)
                    }

                }

                else -> {
                    throw CancellationException("Error - IfEqual")
                }
            }
        }


    }

}