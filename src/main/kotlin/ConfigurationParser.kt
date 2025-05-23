import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.StringReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.annotation.*

class ConfigurationParser {

    companion object {
        var fileName: String = ""
        var readFromResources: Boolean = false
        val logger = LoggerFactory.getLogger("ConfigurationParser")
    }

    fun setReadFromResources(value: Boolean) {
        readFromResources = value
    }

    fun setFileName(file: String) {
        fileName = file
    }
    private fun load(): Device? {
        try {
            val context = JAXBContext.newInstance(Device::class.java, Set::class.java, Toggle::class.java, Random::class.java, Delay::class.java, Linear::class.java, Add::class.java, Sub::class.java, Csv::class.java, IfEqual::class.java, Parameters::class.java, Parameter::class.java, Trace::class.java, Mult::class.java, Div::class.java, IfGreater::class.java, IfLess::class.java)
            val unmarshaller = context.createUnmarshaller()
            return if(fileName.isEmpty() ) {
                val reader = StringReader(this::class.java.classLoader.getResource("configuration.xml")!!.readText())
                val device = unmarshaller.unmarshal(reader) as Device
                device
            }else {
                if(readFromResources) {
                    val reader = StringReader(this::class.java.classLoader.getResource(fileName)!!.readText())
                    val device = unmarshaller.unmarshal(reader) as Device
                    device
                }else {
                    val bufferedReader: BufferedReader = File(fileName).bufferedReader()
                    val device = unmarshaller.unmarshal(bufferedReader) as Device
                    device
                }
            }
        } catch (e: JAXBException) {
            logger.error(e.message)
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
    @field:XmlElement(required = false)
    val parameters: Parameters,
    @field:XmlElement
    val configuration: Configuration,
    @field:XmlElement
    val simulation: Simulation,
    ){
    constructor() : this("", "", Parameters(), Configuration(true,0, Registers(mutableListOf())), Simulation(1000,mutableListOf()))
}

@XmlAccessorType(XmlAccessType.NONE)
class Simulation(
    @field:XmlAttribute(required = true)
    var plcScanTime: Int,
    @XmlAnyElement(lax = true)
    var randomElements: List<Any>
){
    constructor() : this(1000,mutableListOf())

}

//<ifEqual symbol="TEMP" value="12.4">
//   <sub symbol="MOTOR_SPEED1">12</sub>
//   any other operation ...
//</ifEqual>
@XmlRootElement(name="ifEqual")
data class IfEqual(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlAttribute(required = true)
    val value: String,
    @XmlAnyElement(lax = true)
    var randomElements: List<Any>
){
    constructor() : this("","",mutableListOf())
}

//<ifGreater symbol="TEMP" value="12.4">
//   <sub symbol="MOTOR_SPEED1">12</sub>
//   any other operation ...
//</ifGreater>
@XmlRootElement(name="ifGreater")
data class IfGreater(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlAttribute(required = true)
    val value: String,
    @XmlAnyElement(lax = true)
    var randomElements: List<Any>
){
    constructor() : this("","",mutableListOf())
}

//<ifLess symbol="TEMP" value="12.4">
//   <sub symbol="MOTOR_SPEED1">12</sub>
//   any other operation ...
//</ifLess>
@XmlRootElement(name="ifLess")
data class IfLess(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlAttribute(required = true)
    val value: String,
    @XmlAnyElement(lax = true)
    var randomElements: List<Any>
){
    constructor() : this("","",mutableListOf())
}



//<csv symbol="TEMPERATURE_MOTOR4" file="test.csv" column="0" step="2" startRow="2" endRow="100" replay="true"/>
@XmlRootElement(name="csv")
data class Csv(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlAttribute(required = true)
    val file: String,
    @field:XmlAttribute(required = true)
    val column: Int,
    @field:XmlAttribute(required = false)
    val step: Int,
    @field:XmlAttribute(required = false)
    val startRow: Int,
    @field:XmlAttribute(required = false)
    val endRow: Int,
    @field:XmlAttribute(required = false)
    val replay: Boolean
){
    constructor(): this("", "", 0,1,0,-1, false)
}

// <linear symbol="RPM_MOTOR1" a="5" b="3" startX="500" endX="1000" replay="false" step="3"/>
@XmlRootElement(name="linear")
data class Linear(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlAttribute(required = true)
    val a: Double,
    @field:XmlAttribute(required = true)
    val b: Double,
    @field:XmlAttribute(required = true)
    val startX: Double,
    @field:XmlAttribute(required = true)
    val endX: Double,
    @field:XmlAttribute(required = true)
    val replay: Boolean,
    @field:XmlAttribute(required = false)
    val step: Double
){
    constructor(): this("", 0.0, 0.0,0.0,0.0,true, 1.0 )
}

//<sub symbol="MOTOR_SPEED1">12</sub>
@XmlRootElement(name="sub")
data class Sub(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlValue
    val value: String,
){
    constructor(): this("", "")
}


//<trace symbol="MOTOR_SPEED1"></trace>
@XmlRootElement(name="trace")
data class Trace(
    @field:XmlAttribute(required = true)
    val symbol: String,
){
    constructor(): this("")
}



//<add symbol="MOTOR_SPEED1">RPM_MOTOR1</add>
@XmlRootElement(name="add")
data class Add(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlValue
    val value: String,
){
    constructor(): this("", "0")
}

//<mult symbol="MOTOR_SPEED1">RPM_MOTOR1</mult>
@XmlRootElement(name="mult")
data class Mult(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlValue
    val value: String,
){
    constructor(): this("", "0")
}

//<div symbol="MOTOR_SPEED1">RPM_MOTOR1</div>
@XmlRootElement(name="div")
data class Div(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlValue
    val value: String,
){
    constructor(): this("", "0")
}


//<delay>100</delay>
@XmlRootElement(name="delay")
data class Delay(
    @field:XmlValue
    val value: Int
){
    constructor(): this(0)
}

//<random symbol="TEMPERATURE1" valueMin="-50.0" valueMax="10.0"/>
@XmlRootElement(name="random")
data class Random(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlAttribute(required = true)
    val valueMin: Double,
    @field:XmlAttribute(required = true)
    val valueMax: Double,
){
    constructor(): this("", 0.0, 0.0)
}

@XmlRootElement(name="set")
data class Set(
    @field:XmlAttribute(required = true)
    val symbol: String,
    @field:XmlValue
    val value: String
){
    constructor(): this("", "0")
}

//<toggle symbol="MOTOR_SPEED1" />
@XmlRootElement(name="toggle")
data class Toggle(
    @field:XmlAttribute(required = true)
    val symbol: String
){
    constructor(): this("")
}

@XmlAccessorType(XmlAccessType.NONE)
data class Parameters(
    @field:XmlElement(name = "parameter")
    val parameters: MutableList<Parameter>
) {
    constructor(): this(mutableListOf())
    fun getParametersConfiguration(symbolName: String) : Parameter? {
        parameters.forEach {register ->
            if(register.symbol == symbolName){
                return register
            }
        }
        return null
    }
}

@XmlAccessorType(XmlAccessType.NONE)
data class Parameter(
    @XmlAttribute
    val symbol: String,
    @XmlAttribute(required = true)
    val datatype: String,
    @XmlValue
    val value: String
) {
    constructor() : this( "","","")
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

class Registers(
    @field:XmlElement(name = "register")
    val register: MutableList<Register>
) {
    constructor(): this(mutableListOf())
    fun getVarConfiguration(symbolName: String) : Register? {
        register.forEach {register ->
            if(register.symbol == symbolName){
                return register
            }
        }
        return null
    }
}

enum class AddressType(val desc: String) {
    HOLDING_REGISTER("HOLDING_REGISTER"),
    COIL("COIL"),
    DISCRETE_INPUT("DISCRETE_INPUT"),
    INPUT_REGISTER("INPUT_REGISTER")
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