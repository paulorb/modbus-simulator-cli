import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
}