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

