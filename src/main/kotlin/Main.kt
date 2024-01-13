import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.Callable
import kotlin.system.exitProcess


@Command(name = "modbussimulatorcli", mixinStandardHelpOptions = true, version = ["CLI 0.0.99"],
    description = ["Modbus TCP Simulator"])
class Checksum : Callable<Int> {

    @Option(names = ["-f", "--file"], description = ["custom simulation configuration file (JSON)"])
    var file = ""

    @Option(names = ["-p", "--port"], description = ["server TCP port"])
    var port = "502"

    @Option(names = ["-sr", "--simulation-random"], description = ["random number simulation"])
    var simulationRandomValues = false



    private lateinit var plcMemory: PlcMemory
    private lateinit var plcSimulation: PlcSimulation
    private lateinit var modbusServer: ModbusServer

    override fun call(): Int {
        val mainCoroutineScope = CoroutineScope(Dispatchers.Default)
        val configuration = ConfigurationParser()

        if(simulationRandomValues && file.isNotEmpty()){
            println("-f and -sr cannot be mixed, one of the simulations must be chosen")
            return -1
        }
        //val fileContents = Files.readAllBytes(file.toPath())
        //val digest = MessageDigest.getInstance(port).digest(fileContents)
        //println(("%0" + digest.size * 2 + "x").format(BigInteger(1, digest)))
        if(simulationRandomValues) {
            plcMemory = PlcMemory(configuration)
            modbusServer = ModbusServer(plcMemory)
        }else {
            if (file.isNotEmpty()) {
                // -f file based (Custom simulation)
                configuration.setFileName(file)
            }
            // if not set default, reading internal xml
            plcMemory = PlcMemory(configuration)
            plcSimulation = PlcSimulation(configuration, plcMemory, mainCoroutineScope)
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