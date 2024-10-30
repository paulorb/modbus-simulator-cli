import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import operations.*
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException

class PlcSimulation(
    private val configurationParser: ConfigurationParser,
    memory: PlcMemory,
    private val parameters: EnvironmentVariables,
    coroutineScope: CoroutineScope
): BaseOperation(parameters,configurationParser.getConfiguredDevice().configuration, memory ) {
    val linearOperations = LinearOperation()
    val csvOperations = CsvOperation()
    var traceOperation = TraceOperation(configurationParser.getConfiguredDevice().configuration, memory, parameters)
    val addOperation = AddOperation(configurationParser.getConfiguredDevice().configuration, memory, parameters)
    val setOperation = SetOperation(configurationParser.getConfiguredDevice().configuration, memory, parameters)
    val subOperation = SubOperation(configurationParser.getConfiguredDevice().configuration, memory, parameters)
    val multOperation = MultOperation(configurationParser.getConfiguredDevice().configuration, memory, parameters)
    val divOperation = DivOperation(configurationParser.getConfiguredDevice().configuration, memory, parameters)

    companion object {
        val logger = LoggerFactory.getLogger("PlcSimulation")
    }

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
                logger.error("Exception - ${e.message}")
             } finally {
                logger.error("Job: Finally block, cleaning up resources")
            }
        }

    }

    suspend fun processOperationElement(element: Any, configuration: Configuration, memory: PlcMemory) {
        when (element) {
            is Set -> {
                setOperation.setOperation(element)
            }

            is Random -> {
                randomOperation(element, configuration, memory)
            }

            is Delay -> {
                logger.info("Delay value ${element.value}")
                delay(element.value.toLong())
            }

            is Linear -> {
                linearOperations.process(element, configuration, memory)
            }

            is Add -> {
                addOperation.addOperation(element)
            }

            is Sub -> {
                subOperation.subOperation(element)
            }

            is Csv -> {
                csvOperations.process(element, configuration, memory)
            }

            is IfEqual -> {
                ifEqual(element, configuration, memory)
            }

            is IfGreater -> {
                ifGreater(element, configuration, memory)
            }

            is IfLess -> {
                ifLess(element, configuration, memory)
            }

            is Trace -> {
                traceOperation.traceOperation(element)
            }

            is Mult -> {
                multOperation.multOperation(element)
            }

            is Div -> {
                divOperation.divOperation(element)
            }

            else -> throw UnsupportedOperationException("Unknown simulation step type")
        }
    }



    suspend fun ifGreater(element: IfGreater, configuration: Configuration, memory: PlcMemory) {
        logger.info("ifGreater symbol ${element.symbol} value ${element.value}")
        var value = processValue(element.value)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            val envVar = parameters.resolveEnvVar(element.symbol)
            if(envVar != null){
                if(envVar.type == "FLOAT32"){
                    if(envVar.value.toFloat() <= value.toFloat()){
                        //abort and continue
                        //since the value does not match the condition
                        return
                    }
                    //execute operations
                    element.randomElements.forEach { subElement ->
                        processOperationElement(subElement, configuration, memory)
                    }
                }else
                    if(envVar.type == "INT16" || envVar.type == "BOOL" ){
                        if(envVar.value.toInt() <= value.toInt()){
                            //abort and continue
                            //since the value does not match the condition
                            return
                        }
                        //execute operations
                        element.randomElements.forEach { subElement ->
                            processOperationElement(subElement, configuration, memory)
                        }
                    }else {
                        logger.error("Symbol ${element.symbol} has invalid datatype during IfEqual execution")
                    }

            }else {
                logger.error("Symbol ${element.symbol} not found during IfEqual execution")
                throw CancellationException("Error - IfEqual")
            }
        } else {

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //compare
                    //execute operations

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if(currentValue.isEmpty()){
                            logger.error("Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        val intValue = (( currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)

                        //compare
                        if(currentFloatValue <= value.toFloat()){
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
                            logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - IfEqual")
                        }
                        //compare
                        if(currentValue.first().toInt() <= value.toInt()){
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
                        logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                        throw CancellationException("Error - IfEqual")
                    }
                    //compare
                    if( currentValue.first().toShort() <= value.toShort()){
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
                        logger.error("Invalid value format on IfEqual. Symbol ${element.symbol} is of BOOL type and supports only values 0 or 1 not ${element.value} for comparison")
                    }
                    var currentValue = memory.readCoilStatus(variable.address.toInt(), 2)
                    if(currentValue.isEmpty()){
                        logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                        throw CancellationException("Error - IfEqual")
                    }
                    //compare
                    if( currentValue.first() <= value.toBoolean()){
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


    suspend fun ifLess(element: IfLess, configuration: Configuration, memory: PlcMemory) {
        logger.info("ifLess symbol ${element.symbol} value ${element.value}")
        var value = processValue(element.value)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            val envVar = parameters.resolveEnvVar(element.symbol)
            if(envVar != null){
                if(envVar.type == "FLOAT32"){
                    if(envVar.value.toFloat() >= value.toFloat()){
                        //abort and continue
                        //since the value does not match the condition
                        return
                    }
                    //execute operations
                    element.randomElements.forEach { subElement ->
                        processOperationElement(subElement, configuration, memory)
                    }
                }else
                    if(envVar.type == "INT16" || envVar.type == "BOOL" ){
                        if(envVar.value.toInt() >= value.toInt()){
                            //abort and continue
                            //since the value does not match the condition
                            return
                        }
                        //execute operations
                        element.randomElements.forEach { subElement ->
                            processOperationElement(subElement, configuration, memory)
                        }
                    }else {
                        logger.error("Symbol ${element.symbol} has invalid datatype during IfEqual execution")
                    }

            }else {
                logger.error("Symbol ${element.symbol} not found during IfEqual execution")
                throw CancellationException("Error - IfEqual")
            }
        } else {

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //compare
                    //execute operations

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if(currentValue.isEmpty()){
                            logger.error("Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        val intValue = (( currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)

                        //compare
                        if(currentFloatValue >= value.toFloat()){
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
                            logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - IfEqual")
                        }
                        //compare
                        if(currentValue.first().toInt() >= value.toInt()){
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
                        logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                        throw CancellationException("Error - IfEqual")
                    }
                    //compare
                    if( currentValue.first().toShort() >= value.toShort()){
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
                        logger.error("Invalid value format on IfEqual. Symbol ${element.symbol} is of BOOL type and supports only values 0 or 1 not ${element.value} for comparison")
                    }
                    var currentValue = memory.readCoilStatus(variable.address.toInt(), 2)
                    if(currentValue.isEmpty()){
                        logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                        throw CancellationException("Error - IfEqual")
                    }
                    //compare
                    if( currentValue.first() >= value.toBoolean()){
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
    suspend fun ifEqual(element: IfEqual, configuration: Configuration, memory: PlcMemory) {
        logger.info("IfEqual symbol ${element.symbol} value ${element.value}")
        var value = processValue(element.value)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            val envVar = parameters.resolveEnvVar(element.symbol)
            if(envVar != null){
                if(envVar.type == "FLOAT32"){
                    if(envVar.value.toFloat() != value.toFloat()){
                        //abort and continue
                        //since the value does not match the condition
                        return
                    }
                    //execute operations
                    element.randomElements.forEach { subElement ->
                        processOperationElement(subElement, configuration, memory)
                    }
                }else
                    if(envVar.type == "INT16" || envVar.type == "BOOL" ){
                        if(envVar.value.toInt() != value.toInt()){
                            //abort and continue
                            //since the value does not match the condition
                            return
                        }
                        //execute operations
                        element.randomElements.forEach { subElement ->
                            processOperationElement(subElement, configuration, memory)
                        }
                    }else {
                        logger.error("Symbol ${element.symbol} has invalid datatype during IfEqual execution")
                    }

            }else {
                logger.error("Symbol ${element.symbol} not found during IfEqual execution")
                throw CancellationException("Error - IfEqual")
            }
        } else {

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //compare
                    //execute operations

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if(currentValue.isEmpty()){
                            logger.error("Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        val intValue = (( currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)

                        //compare
                        if(currentFloatValue != value.toFloat()){
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
                            logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - IfEqual")
                        }
                        //compare
                        if(currentValue.first().toInt() != value.toInt()){
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
                        logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                        throw CancellationException("Error - IfEqual")
                    }
                    //compare
                    if( currentValue.first().toShort() != value.toShort()){
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
                        logger.error("Invalid value format on IfEqual. Symbol ${element.symbol} is of BOOL type and supports only values 0 or 1 not ${element.value} for comparison")
                    }
                    var currentValue = memory.readCoilStatus(variable.address.toInt(), 2)
                    if(currentValue.isEmpty()){
                        logger.error("IfEqual Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                        throw CancellationException("Error - IfEqual")
                    }
                    //compare
                    if( currentValue.first() != value.toBoolean()){
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