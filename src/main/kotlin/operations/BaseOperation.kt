package operations

import Configuration
import EnvironmentVariables
import PlcMemory
import Register
import isNumeric
import java.rmi.NotBoundException
import java.util.concurrent.CancellationException

abstract class BaseOperation(
    private val parameters: EnvironmentVariables,
    private val configuration: Configuration,
    private val memory: PlcMemory
) {
    /**
     * processValue convert any variable name used as value to the actual value
     * in case no variable name is passed for example in case of passing a numeric value the
     * return is the numeric value itself
     */
    fun processValue(value: String) : String{
        if(isNumeric(value)){
            // numeric value no conversion needed
            return value
        }
        //value is a parameter
        val envVar = parameters.resolveEnvVar(value)
        if(envVar != null){
            return envVar.value
        }
        //value is a symbolic register
        var symbolicVariable = configuration.registers.getVarConfiguration(value)
        if (symbolicVariable != null) {
            return resolveSymbolicVariable(symbolicVariable)
        }
        throw NotBoundException("Invalid value, value is not numeric and also not a parameter nor a symbolic register")
    }

    fun resolveSymbolicVariable(variable: Register) : String{
        when (variable.addressType) {

            AddressType.HOLDING_REGISTER -> {
                //get the current value
                //Mult
                //set back the new value

                if (variable.datatype == "FLOAT32") {
                    var currentValue = memory.readHoldingRegister(variable.address.toInt(), 2)
                    if(currentValue.isEmpty()){
                        throw CancellationException("Error - Base Operation")
                    }
                    val intValue = (( currentValue[1].toInt() shl 16) or (currentValue[0].toInt() and 0xFFFF))
                    return java.lang.Float.intBitsToFloat(intValue).toString()
                } else {
                    var currentValue = memory.readHoldingRegister(variable.address.toInt(), 1)
                    if(currentValue.isEmpty()){
                        throw CancellationException("Error - Base Operation")
                    }
                    return currentValue.first().toInt().toString()
                }
            }
            AddressType.INPUT_REGISTER -> {
                val currentValue = memory.readInputRegister(variable.address.toInt(), 1)
                return currentValue.first().toShort().toString()
            }
            AddressType.COIL -> {
                var currentValue = memory.readCoilStatus(variable.address.toInt(), 2)
                if(currentValue.isEmpty()){
                    throw CancellationException("Error -  Base Operation")
                }
                return currentValue.first().toString()
            }

            else -> {
                throw CancellationException("Error -  Base Operation")
            }
        }
    }
}