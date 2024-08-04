import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap


class PlcMemory(configurationParser: ConfigurationParser)  : IModbusServerEventListener {

    private var coils: ConcurrentHashMap<Int, Boolean> = ConcurrentHashMap()
    private var inputStatus: ConcurrentHashMap<Int, Boolean> = ConcurrentHashMap()
    private var inputRegister: ConcurrentHashMap<Int, Short> = ConcurrentHashMap()
    private var holdingRegister: ConcurrentHashMap<Int, Short> = ConcurrentHashMap()
    private var device =  configurationParser.getConfiguredDevice()

    companion object {
        val logger = LoggerFactory.getLogger("PlcMemory")
    }

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
                    }else if(register.datatype == "UINT16"){
                        holdingRegister[register.address.toInt()] =  register.value.toUShort().toShort()
                    }else
                        holdingRegister[register.address.toInt()] =  register.value.toShort()


                }
                AddressType.COIL -> coils[register.address.toInt()] = (register.value.toInt() == 1)
                AddressType.DISCRETE_INPUT -> inputStatus[register.address.toInt()] =  (register.value.toInt() == 1)
                AddressType.INPUT_REGISTER -> inputRegister[register.address.toInt()] =  register.value.toShort()
            }
        }
    }

    //0x
    override fun forceMultipleCoils(addressValueList: MutableList<Pair<Int, Boolean>>) {
        logger.debug("(0x) forceMultipleCoils")
        addressValueList.forEach { coil ->
            coils[coil.first] = coil.second
        }
    }

    //0x
    override fun forceSingleCoil(address: Int, value: Boolean) {
        logger.debug("(0x) forceSingleCoil")
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
        logger.debug("(4x) presetMultipleRegisters")
        addressValueList.forEach { register ->
            holdingRegister[register.first] = register.second
        }
    }



    override fun presetSingleRegister(address: Int, value: Boolean) {
        logger.debug("presetSingleRegister")
        TODO("Not yet implemented")
    }

    // 0x Registers
    override fun readCoilStatus(startAddress: Int, numberOfRegisters: Int): List<Boolean> {
        logger.debug("(0x) readCoilStatus")
        val listCoils = mutableListOf<Boolean>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(coils[i] != null){
                listCoils.add(coils.getValue(i))
            }else{
                listCoils.add(false)
            }
        }
        return listCoils
    }

    // 4x
    override fun readHoldingRegister(startAddress: Int, numberOfRegisters: Int): List<Short> {
        logger.debug("(4x) readHoldingRegister")
        val listHoldingRegisters = mutableListOf<Short>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(holdingRegister[i] != null){
                logger.debug("readHoldingRegister address $i value=${holdingRegister[i]}")
                listHoldingRegisters.add(holdingRegister.getValue(i))
            }else{
                logger.debug("readHoldingRegister address $i value=0")
                listHoldingRegisters.add(0)
            }
        }
        return listHoldingRegisters
    }

    // 3x register
    override fun readInputRegister(startAddress: Int, numberOfRegisters: Int): List<Short> {
        logger.debug("(3x) readInputRegister")
        val listInputRegisters = mutableListOf<Short>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(inputRegister[i] != null){
                listInputRegisters.add(inputRegister.getValue(i))
            }else{
                listInputRegisters.add(0)
            }
        }
        return listInputRegisters
    }

    // 1x register
    override fun readInputStatus(startAddress: Int, numberOfRegisters: Int): List<Boolean> {
        logger.debug("readInputStatus")
        val listCoils = mutableListOf<Boolean>()
        for(i in startAddress until startAddress + numberOfRegisters) {
            if(inputStatus[i] != null){
                listCoils.add(inputStatus.getValue(i))
            }else{
                listCoils.add(false)
            }
        }
        return listCoils
    }

}