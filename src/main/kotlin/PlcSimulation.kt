import kotlinx.coroutines.*
import operations.*
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
                                randomOperation(element, configuration, memory)
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
                                subOperation(element, configuration, memory)
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