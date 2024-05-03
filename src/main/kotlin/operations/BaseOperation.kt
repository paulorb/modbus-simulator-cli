package operations

import Configuration
import ConfigurationParser
import EnvParameter
import EnvironmentVariables
import isNumeric
import java.rmi.NotBoundException

abstract class BaseOperation(
    private val parameters: EnvironmentVariables,
    private val configuration: Configuration
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
            return symbolicVariable.value
        }
        throw NotBoundException("Invalid value, value is not numeric and also not a parameter nor a symbolic register")

    }
}