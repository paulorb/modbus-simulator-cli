package operations

import Configuration
import PlcMemory
import Add
import EnvironmentVariables
import java.util.concurrent.CancellationException
import toBooleanFromBinary

class AddOperation(private val configuration: Configuration,private val memory: PlcMemory, environmentVariables: EnvironmentVariables
) : BaseOperation(environmentVariables, configuration) {
    fun addOperation(element: Add){
        println("Add symbol ${element.symbol} value ${element.value}")
        var value = processValue(element.value)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            println("ERROR: Symbol ${element.symbol} not found during Set execution")
            throw CancellationException("Error - Add")
        } else {
            if(variable.addressType == AddressType.COIL || variable.addressType == AddressType.DISCRETE_INPUT){
                println("ERROR: Symbol ${element.symbol} is of type COIL or DISCRETE_INPUT which is not support by Add operation")
                throw CancellationException("Error - Add")
            }

            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //add
                    //set back the new value

                    if (variable.datatype == "FLOAT32") {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                        if(currentValue.isEmpty()){
                            println("ERROR: Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        val intValue = (( currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                        val currentFloatValue = java.lang.Float.intBitsToFloat(intValue)
                        var floatValue = value.toFloat()
                        floatValue += currentFloatValue
                        setHoldingRegisterFloat32(floatValue, memory, variable)
                    } else {
                        var currentValue = memory.readHoldingRegister(variable.address.toInt(), 1)
                        if(currentValue.isEmpty()){
                            println("ERROR: Add Operation - Unable to get value of ${element.symbol} address ${variable.address} ")
                            throw CancellationException("Error - Add")
                        }
                        var intValue =  value.toInt()
                        intValue += currentValue.first().toInt()
                        setHoldingRegisterInt16(memory, variable, intValue.toShort())
                    }
                }
                AddressType.INPUT_REGISTER -> {
                    val currentValue = memory.readInputRegister(variable.address.toInt(), 1)
                    val newValue = currentValue.first() + value.toShort()
                    memory.setInputRegister(variable.address.toInt(),newValue.toShort())
                }

                else -> {
                    throw CancellationException("Error - Add")
                }
            }
        }
}

}