import java.util.*

class EnvironmentVariables(
    parameters: List<EnvParameter>,
    private val configurationParser: ConfigurationParser)
{
private var enviromentVars = mutableListOf<EnvParameter>()
init {
    var found = false
    configurationParser.getConfiguredDevice().parameters.parameters.forEach { jsonParameter ->
        found = false
        parameters.forEach { envParameter ->
            if (envParameter.symbol == jsonParameter.symbol) {
                if(jsonParameter.datatype == "INT16" && envParameter.value.toIntOrNull() == null){
                    throw InvalidPropertiesFormatException("Environment variable for parameter ${jsonParameter.symbol} has value ${envParameter.value} which does not conform with its datatype INT16")
                }
                if(jsonParameter.datatype == "FLOAT32" && envParameter.value.toFloatOrNull() == null){
                    throw InvalidPropertiesFormatException("Environment variable for parameter ${jsonParameter.symbol} has value ${envParameter.value} which does not conform with its datatype FLOAT32")
                }
                if(jsonParameter.datatype == "BOOL" && envParameter.value != "0" &&  envParameter.value != "1"){
                    throw InvalidPropertiesFormatException("Environment variable for parameter ${jsonParameter.symbol} has value ${envParameter.value} which does not conform with its datatype BOOL")
                }
                enviromentVars.add(EnvParameter(envParameter.symbol, envParameter.value))
                found = true
            }
        }
        if (!found) {
            if (jsonParameter.value.isEmpty()) {
                throw InvalidPropertiesFormatException("Environment variable for parameter ${jsonParameter.symbol} not found, json definition  do not have a default value, which makes it mandatory")
            } else {
                enviromentVars.add(EnvParameter(jsonParameter.symbol, jsonParameter.value))
            }
        }
    }
}
    fun resolveEnvVar(variableName: String): EnvParameter? {
        enviromentVars.forEach {parameter ->
            if(parameter.symbol == variableName)
                return parameter
        }
        return null
    }
}