import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import java.util.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess
import org.apache.logging.log4j.core.config.Configurator
import org.slf4j.LoggerFactory

data class EnvParameter(
    var symbol : String,
    var value: String,
    var type: String
)



@Command(name = "modbussimulatorcli", mixinStandardHelpOptions = true, version = ["CLI 0.0.99"],
    description = ["Modbus TCP Simulator"], header = [
"                     _ _                _____ _ _",
"                    | | |              / ____| (_)",
" _ __ ___   ___   __| | |__  _   _ ___| |    | |_",
"| '_ ` _ \\ / _ \\ / _` | '_ \\| | | / __| |    | | |",
"| | | | | | (_) | (_| | |_) | |_| \\__ \\ |____| | |",
"|_| |_| |_|\\___/ \\__,_|_.__/ \\__,_|___/\\_____|_|_|",
""
]
)
class Checksum : Callable<Int> {

    @Spec
    var spec: CommandSpec? = null

    @Option(names = ["-f", "--file"], description = ["custom simulation configuration file (JSON)"])
    var file = ""

    @Option(names = ["-p", "--port"], description = ["server TCP port"])
    var port = "502"

    @Option(names = ["-sr", "--simulation-random"], description = ["random number simulation"])
    var simulationRandomValues = false

    //-e CUSTOM_VALUE=12 -e MOTOR_VAL=12.2
    @Option(names = ["-e", "--env"], description = ["environment variables"])
    var parameters: MutableList<String?>? = null

    private lateinit var plcMemory: PlcMemory
    private lateinit var plcSimulation: PlcSimulation
    private lateinit var modbusServer: ModbusServer
    private lateinit var environmentParameters: List<EnvParameter>

    companion object {
        val logger = LoggerFactory.getLogger("main")
    }

    private fun processEnvironmentParameters(parameters: MutableList<String?>?): List<EnvParameter> {
        val envParameter = mutableListOf<EnvParameter>()
        parameters?.forEach { param ->
            val parts = param?.split('=')
            if (parts != null) {
                if(parts.count() != 2){
                    throw InvalidPropertiesFormatException("Environment parameter does not follow the format NAME=VALUE")
                }else{
                    envParameter.add(EnvParameter(parts[0],parts[1], ""))
                }
            }
        }
        return envParameter.toList()
    }

    override fun call(): Int {
        Configurator.initialize(null, "log4j2.xml")
        val mainCoroutineScope = CoroutineScope(Dispatchers.Default)
        val configuration = ConfigurationParser()

        if(simulationRandomValues && file.isNotEmpty()){
            logger.error("-f and -sr cannot be mixed, one of the simulations must be chosen")
            return -1
        }

        environmentParameters = processEnvironmentParameters(parameters)
        if(environmentParameters.isNotEmpty()){
            logger.warn("environment parameters: ${environmentParameters.toString()}")
        }

        //val fileContents = Files.readAllBytes(file.toPath())
        //val digest = MessageDigest.getInstance(port).digest(fileContents)
        //println(("%0" + digest.size * 2 + "x").format(BigInteger(1, digest)))
        if(simulationRandomValues) {
            modbusServer = ModbusServer(ModbusServerEventListenerReplyRandomNumbers())
        }else {
            if (file.isNotEmpty()) {
                // -f file based (Custom simulation)
                configuration.setFileName(file)
            }
            // if not set default, reading internal xml
            plcMemory = PlcMemory(configuration)
            var envVariables = EnvironmentVariables(environmentParameters, configuration)
            plcSimulation = PlcSimulation(configuration, plcMemory,envVariables, mainCoroutineScope)
            modbusServer = ModbusServer(plcMemory)
        }

        try {
            modbusServer.start()
            modbusServer.block()
        }catch (ex: Exception){
            modbusServer.stop()
        }
        return 0
    }


}

fun main(args: Array<String>) : Unit = exitProcess(CommandLine(Checksum()).execute(*args))