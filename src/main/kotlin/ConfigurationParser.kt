import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import javax.xml.stream.XMLInputFactory

class ConfigurationParser {
   private var xmlDeserializer : ObjectMapper = XmlMapper(JacksonXmlModule().apply {
       setDefaultUseWrapper(false)
   }).registerKotlinModule()
       .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
       .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private fun load(): Device? {
        return xmlDeserializer.readValue(this::class.java.classLoader.getResource("configuration.xml")!!.readText(), Device::class.java)
    }

    fun getConfiguredDevice() : Device {
        return load()!!
    }

}


@JacksonXmlRootElement
class Device(
    @JacksonXmlProperty(isAttribute = true)
    var address: String,
    @JacksonXmlProperty(isAttribute = true)
    var port: String,
    val configuration: Configuration
    )


class Configuration(
    val initializeUndefinedRegisters: Boolean,
    val initialValue: Int,
    val registers : Registers
)

class Registers(
    val register: List<Register>
)

enum class AddressType(val desc: String) {
    HOLDING_REGISTER("HOLDING_REGISTER"),
    COIL("COIL")
}

class Register(
    @JacksonXmlProperty(isAttribute = true)
    val addressType: AddressType,
    @JacksonXmlProperty(isAttribute = true)
    val address: String,
    @JacksonXmlProperty(isAttribute = true)
    val symbol: String,
    @JacksonXmlText(value = true)
    @JacksonXmlProperty(localName="Register")
    @JsonProperty("Register")
    val value: String
)