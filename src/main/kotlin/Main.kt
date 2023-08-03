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

    @Option(names = ["-f", "--file"], description = ["configuration file (JSON)"])
    lateinit var file: File

    @Option(names = ["-p", "--port"], description = ["server TCP port"])
    var port = "502"



    override fun call(): Int {
        //val fileContents = Files.readAllBytes(file.toPath())
        //val digest = MessageDigest.getInstance(port).digest(fileContents)
        //println(("%0" + digest.size * 2 + "x").format(BigInteger(1, digest)))
        var modbusServer = ModbusServer(ModbusServerEventListenerReplyRandomNumbers())
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