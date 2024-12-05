import operations.ToggleOperation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToggleOperationTest {

    @Test
    fun `Toggle must invert the boolean value`() {
        val configuration = ConfigurationParser()
        configuration.setReadFromResources(true)
        configuration.setFileName("toggle_operation.xml")
        val plcMemory = PlcMemory(configuration)
        val toggleOperation = ToggleOperation(configuration.getConfiguredDevice().configuration, plcMemory, EnvironmentVariables(listOf<EnvParameter>(),ConfigurationParser() ))
        assertTrue(configuration.getConfiguredDevice().simulation.randomElements[0] is Toggle)
        assertEquals(plcMemory.readCoilStatus(configuration.getConfiguredDevice().configuration.registers.register.first().address.toInt(), 1).first(), false)
        toggleOperation.toggleOperation(configuration.getConfiguredDevice().simulation.randomElements[0] as Toggle)
        assertEquals(plcMemory.readCoilStatus(configuration.getConfiguredDevice().configuration.registers.register.first().address.toInt(), 1).first(), true)
    }
}