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

    @Test
    fun `Set must set a INT16 value to a holding register variable`() {
        val configuration = ConfigurationParser()
        configuration.setReadFromResources(true)
        configuration.setFileName("set_operation.xml")
        val plcMemory = PlcMemory(configuration)
        val setOperation = SetOperation(configuration.getConfiguredDevice().configuration, plcMemory, EnvironmentVariables(listOf<EnvParameter>(),ConfigurationParser() ))
        assertTrue(configuration.getConfiguredDevice().simulation.randomElements[1] is Set)
        assertEquals(plcMemory.readHoldingRegister(configuration.getConfiguredDevice().configuration.registers.register[1].address.toInt(), 1).first(), 500)
        setOperation.setOperation(configuration.getConfiguredDevice().simulation.randomElements[1] as Set)
        assertEquals(plcMemory.readHoldingRegister(configuration.getConfiguredDevice().configuration.registers.register[1].address.toInt(), 1).first(), 600)
    }

    @Test
    fun `Set must set a FLOAT32 value to a holding register variable`() {
        val configuration = ConfigurationParser()
        configuration.setReadFromResources(true)
        configuration.setFileName("set_operation.xml")
        val plcMemory = PlcMemory(configuration)
        val setOperation = SetOperation(configuration.getConfiguredDevice().configuration, plcMemory, EnvironmentVariables(listOf<EnvParameter>(),ConfigurationParser() ))
        assertTrue(configuration.getConfiguredDevice().simulation.randomElements[2] is Set)
        var value1 = plcMemory.readHoldingRegister(configuration.getConfiguredDevice().configuration.registers.register[2].address.toInt(), 2).first()
        var value2 = plcMemory.readHoldingRegister(configuration.getConfiguredDevice().configuration.registers.register[2].address.toInt(), 2)[1]
        var intValue = (( value2.toInt() shl 16) or (value1.toInt() and 0xFFFF))
        var currentFloatValue = java.lang.Float.intBitsToFloat(intValue)
        assertEquals(currentFloatValue, 500.0.toFloat())
        setOperation.setOperation(configuration.getConfiguredDevice().simulation.randomElements[2] as Set)
        value1 = plcMemory.readHoldingRegister(configuration.getConfiguredDevice().configuration.registers.register[2].address.toInt(), 2).first()
        value2 = plcMemory.readHoldingRegister(configuration.getConfiguredDevice().configuration.registers.register[2].address.toInt(), 2)[1]
        intValue = (( value2.toInt() shl 16) or (value1.toInt() and 0xFFFF))
        currentFloatValue = java.lang.Float.intBitsToFloat(intValue)
        assertEquals(currentFloatValue, 600.0.toFloat())
    }


}