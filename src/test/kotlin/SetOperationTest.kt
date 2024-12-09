import operations.SetOperation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetOperationTest {
    @Test
    fun `Set must set a value to a coil variable`() {
        val configuration = ConfigurationParser()
        configuration.setReadFromResources(true)
        configuration.setFileName("set_operation.xml")
        val plcMemory = PlcMemory(configuration)
        val setOperation = SetOperation(configuration.getConfiguredDevice().configuration, plcMemory, EnvironmentVariables(listOf<EnvParameter>(),ConfigurationParser() ))
        assertTrue(configuration.getConfiguredDevice().simulation.randomElements[0] is Set)
        assertEquals(plcMemory.readCoilStatus(configuration.getConfiguredDevice().configuration.registers.register.first().address.toInt(), 1).first(), false)
        setOperation.setOperation(configuration.getConfiguredDevice().simulation.randomElements[0] as Set)
        assertEquals(plcMemory.readCoilStatus(configuration.getConfiguredDevice().configuration.registers.register.first().address.toInt(), 1).first(), true)
    }
}