# Operations

This sections covers the different types of operations the simulator can perform
when configured in advanced (custom) simulation.

## Set
Set (as the name implies) set a certain value to a variable, like the example below:

```xml
 <set symbol="MOTOR_SPEED1">100.5</set>
```

Both **symbol** and **value** are mandatory fields. **value** can be of type *FLOAT32* or *INT16* which must
follow the same type of the specified **symbol** definition. In case the variable already is initialized, it overwrites the value.