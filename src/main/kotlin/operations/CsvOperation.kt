package operations

import Configuration
import Csv
import PlcMemory
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileReader
import java.nio.file.Paths
import java.util.concurrent.CancellationException

data class CsvOperationInfo(var position: Int, var csvColumn: List<String>)
class CsvOperation {
    private var csvVariables: MutableMap<String, CsvOperationInfo> = mutableMapOf<String, CsvOperationInfo>()

    @Throws(InternalError::class)
    private  fun parseCsv(csvFileName: String, column: Int) : List<String> {
        val columnValues = mutableListOf<String>()
        try {
            var newPath = Paths.get(Paths.get(ConfigurationParser.fileName).parent.toString(), csvFileName)
            FileReader( newPath.toString()).use { fileReader ->
                val csvParser = CSVParser(fileReader, CSVFormat.DEFAULT)
                for (csvRecord in csvParser) {
                    columnValues.add( csvRecord.get(column))
                }
            }
        } catch (e: Exception) {
            throw InternalError("Error reading csv file")
        }
        return columnValues
    }


    private fun getNextValue(csv: Csv): String {
        if(!csvVariables.containsKey(csv.symbol)){
            csvVariables[csv.symbol] = CsvOperationInfo(csv.startRow,parseCsv(csv.file,csv.column))
        }
        val currentValue = csvVariables[csv.symbol]!!.csvColumn[csvVariables[csv.symbol]!!.position]
        csvVariables[csv.symbol]!!.position++
        if(csvVariables[csv.symbol]!!.position > csvVariables[csv.symbol]!!.csvColumn.size -1 ||
            (csv.endRow != -1 && csvVariables[csv.symbol]!!.position > csv.endRow)
            ){
            if(csv.replay){
                csvVariables[csv.symbol]!!.position = csv.startRow
            }else{
                csvVariables[csv.symbol]!!.position--
            }
        }
        return currentValue
    }
    fun process(element: Csv, configuration: Configuration, memory: PlcMemory) {
        var nextValue = getNextValue(element)
        var variable = configuration.registers.getVarConfiguration(element.symbol)
        if (variable == null) {
            println("ERROR: Symbol ${element.symbol} not found during CSV execution")
            throw CancellationException("Error - CSV")
        } else {
            when (variable.addressType) {

                AddressType.HOLDING_REGISTER -> {
                    //get the current value
                    //add
                    //set back the new value

                    if (variable.datatype == "FLOAT32") {

                        setHoldingRegisterFloat32(nextValue.toFloat(), memory, variable)
                    } else {

                        setHoldingRegisterInt16(memory, variable, nextValue.toFloat().toInt().toShort())
                    }
                }

                AddressType.INPUT_REGISTER -> {
                    memory.setInputRegister(variable.address.toInt(), nextValue.toFloat().toInt().toShort())
                }

                else -> {
                    throw CancellationException("Error - Linear")
                }
            }
        }
    }

}