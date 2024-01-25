package operations

import Configuration
import PlcMemory
import Linear
import java.util.concurrent.CancellationException
import toBooleanFromBinary
class LinearOperation {
    private var linearVariables: MutableMap<String, Double> = mutableMapOf<String, Double>()
    fun getNextValue(linear: Linear): String{
        val x = linearVariables.getOrDefault(linear.symbol, linear.minX)
        if(x + linear.step < linear.maxX) {
            linearVariables[linear.symbol] = x + linear.step
        }else{
            if(linear.replay){
                linearVariables[linear.symbol] = linear.minX
            }
        }
        return (linear.a * x + linear.b).toString()
    }
}