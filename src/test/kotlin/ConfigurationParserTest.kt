import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

class ConfigurationParserTest {

    // When parser fails to load the xml due to missing device root section the following error will be printed followed by the stacktrace
    // 2024-07-23 21:21:04 ERROR ConfigurationParser - unexpected element (uri:"", local:"simulation"). Expected elements are <{}add>,<{}csv>,<{}delay>,<{}device>,<{}ifEqual>,<{}linear>,<{}random>,<{}set>,<{}sub>
    @Test
    fun `Parser must fail if device section is not present`() {
        val configuration = ConfigurationParser()
        configuration.setReadFromResources(true)
        configuration.setFileName("invalid_missing_device.xml")
        assertThrows<NullPointerException> { configuration.getConfiguredDevice() }
    }

    @Test
    fun `Parse must be able to parse toggle operations`() {
        val configuration = ConfigurationParser()
        configuration.setReadFromResources(true)
        configuration.setFileName("toggle_operation.xml")
        assertTrue(configuration.getConfiguredDevice().simulation.randomElements[0] is Toggle)
    }
}