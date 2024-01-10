import java.util.concurrent.ConcurrentHashMap


class PlcMemory(configurationParser: ConfigurationParser)  : IModbusServerEventListener {

    private var coils: ConcurrentHashMap<Int, Boolean> = ConcurrentHashMap()
    private var inputStatus: ConcurrentHashMap<Int, Boolean> = ConcurrentHashMap()
    private var inputRegister: ConcurrentHashMap<Int, Short> = ConcurrentHashMap()
    private var holdingRegister: ConcurrentHashMap<Int, Short> = ConcurrentHashMap()
    private var device =  configurationParser.getConfiguredDevice()

    init {
        device.configuration.registers.register.forEach { register ->
            when(register.addressType){
                AddressType.HOLDING_REGISTER -> {
                    if(register.datatype == "FLOAT32"){
                        val floatValue = register.value.toFloat()
                        val intValue = java.lang.Float.floatToIntBits(floatValue)
                        val lowWord = intValue and 0xFFFF
                        val highWord = (intValue ushr 16) and 0xFFFF
                        holdingRegister[register.address.toInt()] =  lowWord.toShort()
                        holdingRegister[register.address.toInt() + 1] =  highWord.toShort()
                    }else
                        holdingRegister[register.address.toInt()] =  register.value.toShort()
                }
                AddressType.COIL -> coils[register.address.toInt()] = (register.value.toInt() == 1)
                AddressType.DISCRETE_INPUT -> inputStatus[register.address.toInt()] =  (register.value.toInt() == 1)
                AddressType.INPUT_REGISTER -> inputRegister[register.address.toInt()] =  register.value.toShort()
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

    //0x
    override fun forceMultipleCoils(addressValueList: MutableList<Pair<Int, Boolean>>) {
        println("(0x) forceMultipleCoils")
        addressValueList.forEach { coil ->
            coils[coil.first] = coil.second
        }
    }

    //0x
    override fun forceSingleCoil(address: Int, value: Boolean) {
        println("(0x) forceSingleCoil")
        coils[address] = value
    }

    //1x
    fun setDiscreteInput(address: Int, value: Boolean) {
        inputStatus[address] = value
    }

    //3x
    fun setInputRegister(address: Int, value: Short){
        inputRegister[address] = value
    }

    // 4x
    override fun presetMultipleRegisters(addressValueList: MutableList<Pair<Int, Short>>) {
        println("(4x) presetMultipleRegisters")
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
        println("(0x) readCoilStatus")
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
        println("(4x) readHoldingRegister")
        val listHoldingRegisters = mutableListOf<Short>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(holdingRegister[i] != null){
                println("readHoldingRegister address $i value=${holdingRegister[i]}")
                listHoldingRegisters.add(holdingRegister[i]!!)
            }else{
                println("readHoldingRegister address $i value=0")
                listHoldingRegisters.add(0)
            }
        }
        return listHoldingRegisters
    }

    // 3x register
    override fun readInputRegister(startAddress: Int, numberOfRegisters: Int): List<Short> {
        println("(3x) readInputRegister")
        val listInputRegisters = mutableListOf<Short>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(inputRegister[i] != null){
                listInputRegisters.add(inputRegister[i]!!)
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