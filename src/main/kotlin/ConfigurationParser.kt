import java.io.StringReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.annotation.*

class ConfigurationParser {

    private fun load(): Device? {
        try {
            val context = JAXBContext.newInstance(Device::class.java)
            val unmarshaller = context.createUnmarshaller()
            val reader = StringReader(this::class.java.classLoader.getResource("configuration.xml")!!.readText())
            val device = unmarshaller.unmarshal(reader) as Device
            return device
        } catch (e: JAXBException) {
            e.printStackTrace()
        }
        return null
    }

    fun getConfiguredDevice() : Device {
        return load()!!
    }
}


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
data class Device(
    @field:XmlAttribute(required = false)
    var ip: String,
    @field:XmlAttribute(required = false)
    var port: String,
    @field:XmlElement
    val configuration: Configuration){
    constructor() : this("", "", Configuration(true,0, Registers(mutableListOf())))
}




data class Configuration(
    @field:XmlAttribute(required = false)
    val initializeUndefinedRegisters: Boolean,
    @field:XmlAttribute(required = false)
    val initialValue: Int,
    @field:XmlElement
    val registers : Registers
) {
    constructor(): this(true, 0, Registers(mutableListOf()))
}

data class Registers(
    @field:XmlElement(name = "register")
    val register: MutableList<Register>
) {
    constructor(): this(mutableListOf())
}

enum class AddressType(val desc: String) {
    HOLDING_REGISTER("HOLDING_REGISTER"),
    COIL("COIL")
}

//<register addressType="HOLDING_REGISTER" address="200"  datatype="UINT16" symbol="RPM_MOTOR1">500</register>
//<register addressType="COIL" address="10" symbol="RELAYON">1</register>
data class Register(
    @XmlAttribute
    val addressType: AddressType,
    @XmlAttribute
    val address: String,
    @XmlAttribute(required = false)
    val datatype: String,
    @XmlAttribute
    val symbol: String,
    @XmlValue
    val value: String
) {
    constructor() : this(AddressType.COIL, "","","","")
}