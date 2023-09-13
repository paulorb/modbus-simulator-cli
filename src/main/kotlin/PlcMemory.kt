import java.util.concurrent.ConcurrentHashMap

class PlcMemory(configurationParser: ConfigurationParser)  : IModbusServerEventListener {

    var coils: ConcurrentHashMap<Int, Boolean> = ConcurrentHashMap()
    var inputStatus: ConcurrentHashMap<Int, Int> = ConcurrentHashMap()
    var register: ConcurrentHashMap<Int, Short> = ConcurrentHashMap()
    var holdingRegister: ConcurrentHashMap<Int, Int> = ConcurrentHashMap()
    var device =  configurationParser.getConfiguredDevice()

    init {
        device.configuration.registers.register.forEach { register ->
            when(register.addressType){
                AddressType.HOLDING_REGISTER -> holdingRegister[register.address.toInt()] =  register.value.toInt()
                AddressType.COIL -> coils[register.address.toInt()] = register.value.toBoolean()
            }
        }
    }
    override fun forceMultipleCoils(addressValueList: MutableList<Pair<Int, Boolean>>) {
        addressValueList.forEach { coil ->
            coils[coil.first] = coil.second
        }
    }

    override fun forceSingleCoil(address: Int, value: Boolean) {
        coils[address] = value
    }

    override fun presetMultipleRegisters(addressValueList: MutableList<Pair<Int, Short>>) {
        TODO("Not yet implemented")
    }

    override fun presetSingleRegister(address: Int, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun readCoilStatus(startAddress: Int, numberOfRegisters: Int): List<Boolean> {
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

    override fun readHoldingRegister(startAddress: Int, numberOfRegisters: Int): List<Short> {
        TODO("Not yet implemented")
    }

    override fun readInputRegister(startAddress: Int, numberOfRegisters: Int): List<Short> {
        TODO("Not yet implemented")
    }

    override fun readInputStatus(startAddress: Int, numberOfRegisters: Int): List<Boolean> {
        TODO("Not yet implemented")
    }

}