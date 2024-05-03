import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.InvalidPropertiesFormatException
import java.util.concurrent.Callable
import kotlin.system.exitProcess

data class EnvParameter(
    var symbol : String,
    var value: String
)
@Command(name = "modbussimulatorcli", mixinStandardHelpOptions = true, version = ["CLI 0.0.99"],
    description = ["Modbus TCP Simulator"])
class Checksum : Callable<Int> {

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

    private fun processEnvironmentParameters(parameters: MutableList<String?>?): List<EnvParameter> {
        val envParameter = mutableListOf<EnvParameter>()
        parameters?.forEach { param ->
            val parts = param?.split('=')
            if (parts != null) {
                if(parts.count() != 2){
                    throw InvalidPropertiesFormatException("Environment parameter does not follow the format NAME=VALUE")
                }else{
                    envParameter.add(EnvParameter(parts[0],parts[1]))
                }
            }
        }
        return envParameter.toList()
    }

    override fun call(): Int {
        val mainCoroutineScope = CoroutineScope(Dispatchers.Default)
        val configuration = ConfigurationParser()

        if(simulationRandomValues && file.isNotEmpty()){
            println("-f and -sr cannot be mixed, one of the simulations must be chosen")
            return -1
        }

        environmentParameters = processEnvironmentParameters(parameters)

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
            plcSimulation = PlcSimulation(configuration, plcMemory,EnvironmentVariables(environmentParameters, configuration), mainCoroutineScope)
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