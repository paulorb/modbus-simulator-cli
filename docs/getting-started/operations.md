# Operations

This sections covers the different types of operations the simulator can perform
when configured in advanced (custom) simulation.

## Set
Set (as the name implies) set a certain value to a variable, like the example below:

```xml
 <set symbol="MOTOR_SPEED1">100.5</set>
```

Both **symbol** and **value** are mandatory fields. **value** can be of type *FLOAT32* , *INT16* or *BOOL* which must
follow the same type of the specified **symbol** definition. In case the variable already is initialized, it overwrites the value.

Supported registers: **HOLDING_REGISTER**, **COIL**, **DISCRETE_INPUT**, **INPUT_REGISTER**

## Add
Add (as the name implies) add a certain value to a variable, like the example below:

```xml
 <add symbol="MOTOR_SPEED1">15.5</add>
```

Both **symbol** and **value** are mandatory fields. **value** can be of type *FLOAT32* , *INT16*  which must
follow the same type of the specified **symbol** definition.

Supported registers: **HOLDING_REGISTER**, **INPUT_REGISTER** 

*For **INPUT_REGISTER**  type *FLOAT32* is not supported!*


## Sub
Sub (as the name implies) subtract a certain value of a variable, like the example below:

```xml
 <sub symbol="MOTOR_SPEED1">1.12</sub>
```

Both **symbol** and **value** are mandatory fields. **value** can be of type *FLOAT32* , *INT16*  which must
follow the same type of the specified **symbol** definition.

Supported registers: **HOLDING_REGISTER**, **INPUT_REGISTER**

*For **INPUT_REGISTER**  type *FLOAT32* is not supported!*


## Random
Random generates new random values each time it executes

```xml
 <random symbol="TEMPERATURE1" valueMin="-50.0" valueMax="10.0"/>
```

Both **symbol** and **valueMin** and **valueMax** are mandatory fields. Both values can be of type *FLOAT32* , *INT16*  which must
follow the same type of the specified **symbol** definition.

Supported registers: **HOLDING_REGISTER**, **INPUT_REGISTER**

*For **INPUT_REGISTER**  type *FLOAT32* is not supported!*


## Linear
Generates new values following a linear equation (ax + b)

```
<linear symbol="TEMPERATURE_MOTOR1" a="3" b="2" startX="0" endX="12" replay="true" step="1.5"/>
<linear symbol="TEMPERATURE_MOTOR2" a="3" b="2" startX="12" endX="0" replay="true" step="1.5"/>
```

Both **symbol** , **startX** , **endX**, **a**, **b** and **step** are mandatory fields. Both values can be of type *FLOAT32* , *INT16*  which must
follow the same type of the specified **symbol** definition.
**step** must be a positive value of type INT16 or FLOAT32

On the first example (xml above) x is starting in 0 and going up until 12 (<=) with a step of 1.5
so the x values for the equation will be [0 1.5 3 4.5 6 7.5 9 10.5 12].

Second example is the oposite, note that startX is greater than endX, so the x values applied on this equation will be
[12 10.5 9 7.5 6 4.5 3 1.5 0], also note that the **step** is positive, what is different is the **startX** and **endX** to indicate order.

Supported registers: **HOLDING_REGISTER**, **INPUT_REGISTER**

*For **INPUT_REGISTER**  type *FLOAT32* is not supported!*


## Csv
Uses a CSV as data source based on the specified column.

```
<csv symbol="TEMPERATURE_MOTOR3" file="test_data.csv" column="1" replay="true"/>
<csv symbol="TEMPERATURE_MOTOR4" file="test_data.csv" column="2" step="2" startRow="2" endRow="5" replay="true"/>
```

Both **symbol** , **file** , **column** are mandatory fields. 

**column** must have the number of the column to be read, please notice that the values on the CSV must
follow the same type of the specified **symbol** definition.
**step** must be a positive value of type INT16 which is related to the row index

Each time this operation is executed it will get the next row of the column specified and set to the specified symbol. 

Supported registers: HOLDING_REGISTER, INPUT_REGISTER

For INPUT_REGISTER type FLOAT32 is not supported!

## If equal
Used for comparison, every operation inside the ifequal body will be executed

```
  <ifEqual symbol="TEMPERATURE_MOTOR5" value="-12.5">
            <set symbol="MOTOR5_RPM">0</set>        
  </ifEqual>
```

Both **symbol** and **value** are mandatory fields. Both can have an atomic value or a reference to a symbol (which can refer to a register or a parameter)

Supported registers: HOLDING_REGISTER, INPUT_REGISTER, COIL and PARAMETER

Example using a parameter as a value
```
  <ifEqual symbol="TEMPERATURE_MOTOR5" value="PARAM_DEFINED_TEMPERATURE">
            <set symbol="MOTOR5_RPM">0</set>        
  </ifEqual>
```

Please note the **symbol** and **value** datatype needs to match.