import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class EnvironmentVariablesTest {
    @Test
    fun `Command line parameter must match with parameters definition`() {
        val configuration = ConfigurationParser()
        val listParameters = mutableListOf<EnvParameter>()
        listParameters.add(EnvParameter(
            "PARAM_INT16",
            "1000",
            ""
        ))
        val envVariables = EnvironmentVariables(listParameters, configuration)
        val envVarResolved = envVariables.resolveEnvVar("PARAM_INT16")
        assertEquals("PARAM_INT16", envVarResolved!!.symbol)
        assertEquals("INT16", envVarResolved.type)
        assertEquals("1000", envVarResolved.value)
    }
}