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
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import javax.xml.stream.XMLInputFactory

class ConfigurationParser {
   // private lateinit var xmlDeserializer : ObjectMapper
   // private lateinit var device: Device

    fun initialize(){
        val xmlDeserializer = XmlMapper(JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
        }).registerKotlinModule()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val finalObject = xmlDeserializer.readValue(this::class.java.classLoader.getResource("configuration.xml")!!.readText(), Device::class.java)
        println(finalObject.address)

    }

    fun load() {
    //    val configurationRaw = this::class.java.classLoader.getResource("configuration.xml")!!.readText()
      //  var test = xmlDeserializer.readValue(configurationRaw, Device::class.java)
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


data class Configuration(
    val initializeUndefinedRegisters: Boolean,
    val initialValue: Int,
    val registers : Registers
)

data class Registers(
    val register: List<Register>
)

enum class AddressType(val desc: String) {
    HOLDING_REGISTER("HOLDING_REGISTER"),
    COIL("COIL")
}

data class Register(val addressType: AddressType, val address: String, val symbol: String)