import java.util.concurrent.ConcurrentHashMap
import javax.xml.bind.JAXBElement




class PlcMemory(configurationParser: ConfigurationParser)  : IModbusServerEventListener {

    var coils: ConcurrentHashMap<Int, Boolean> = ConcurrentHashMap()
    var inputStatus: ConcurrentHashMap<Int, Boolean> = ConcurrentHashMap()
    var register: ConcurrentHashMap<Int, Short> = ConcurrentHashMap()
    var holdingRegister: ConcurrentHashMap<Int, Short> = ConcurrentHashMap()
    var device =  configurationParser.getConfiguredDevice()

    init {
        device.configuration.registers.register.forEach { register ->
            when(register.addressType){
                AddressType.HOLDING_REGISTER -> {
                    if(register.datatype == "FLOAT32"){
                        NotImplementedError("FLOAT32 is not supported")
                    }else
                        holdingRegister[register.address.toInt()] =  register.value.toShort()
                }
                AddressType.COIL -> coils[register.address.toInt()] = register.value.toBoolean()
            }
        }
        var simulationElements = device.simulation.randomElements
        simulationElements?.forEach {element ->
            when(element){
                is Set -> {
                    println("Set symbol ${element.symbol} value ${element.value}")
                }
                is Random -> {
                    println("Random symbol ${element.symbol} valueMax ${element.valueMax} valueMin ${element.valueMin}")
                }
                is Delay -> {
                    println("Delay value ${element.value}")
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
    }
    override fun forceMultipleCoils(addressValueList: MutableList<Pair<Int, Boolean>>) {
        println("forceMultipleCoils")
        addressValueList.forEach { coil ->
            coils[coil.first] = coil.second
        }
    }

    override fun forceSingleCoil(address: Int, value: Boolean) {
        println("forceSingleCoil")
        coils[address] = value
    }

    // 4x
    override fun presetMultipleRegisters(addressValueList: MutableList<Pair<Int, Short>>) {
        println("presetMultipleRegisters")
        addressValueList.forEach { register ->
            holdingRegister[register.first] = register.second
        }
    }



    override fun presetSingleRegister(address: Int, value: Boolean) {
        println("presetSingleRegister")
        TODO("Not yet implemented")
    }

    // 0x Registers
    override fun readCoilStatus(startAddress: Int, numberOfRegisters: Int): List<Boolean> {
        println("readCoilStatus")
        val listCoils = mutableListOf<Boolean>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(coils[i] != null){
                listCoils.add(coils[i]!!)
            }else{
                listCoils.add(false)
            }
        }
        return listCoils
    }

    // 4x
    override fun readHoldingRegister(startAddress: Int, numberOfRegisters: Int): List<Short> {
        println("readHoldingRegister")
        val listHoldingRegisters = mutableListOf<Short>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(holdingRegister[i] != null){
                listHoldingRegisters.add(holdingRegister[i]!!)
            }else{
                listHoldingRegisters.add(0)
            }
        }
        return listHoldingRegisters
    }

    // 3x register
    override fun readInputRegister(startAddress: Int, numberOfRegisters: Int): List<Short> {
        println("readInputRegister")
        val listInputRegisters = mutableListOf<Short>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(register[i] != null){
                listInputRegisters.add(register[i]!!)
            }else{
                listInputRegisters.add(0)
            }
        }
        return listInputRegisters
    }

    // 1x register
    override fun readInputStatus(startAddress: Int, numberOfRegisters: Int): List<Boolean> {
        println("readInputStatus")
        val listCoils = mutableListOf<Boolean>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(inputStatus[i] != null){
                listCoils.add(inputStatus[i]!!)
            }else{
                listCoils.add(false)
            }
        }
        return listCoils
    }

}