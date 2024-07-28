import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class PlcMemoryTest {

    @Test
    fun `PlcMemory must be initialized with configured holding registers symbols`() {
        val configuration = ConfigurationParser()
        configuration.setReadFromResources(true)
        configuration.setFileName("configuration_init.xml")
        val plcMemory = PlcMemory(configuration)
        val values = plcMemory.readHoldingRegister(14, 1)
        assertEquals(1, values.size, )
        assertEquals( values[0], 500)
    }

    @Test
    fun `PlcMemory must be initialized with configured input registers symbols`() {
        val configuration = ConfigurationParser()
        configuration.setReadFromResources(true)
        configuration.setFileName("configuration_init.xml")
        val plcMemory = PlcMemory(configuration)
        val values = plcMemory.readInputRegister(5, 1)
        assertEquals(1, values.size, )
        assertEquals( values[0], 30)
    }
}