# Input parameters

Simulator supports input parameters, these parameters are defined on the xml file with optional default values which can be customized by command line arguments.

## How to configure the parameters?

1. Adding parameters section to the XML
2. Define the parameters and its optional default value (in case the default value is not specified this parameter becomes mandatory)

```xml
<?xml version="1.0" encoding="US-ASCII" ?>
<device ip="0.0.0.0" port="502">
    <parameters>
        <parameter symbol="PARAM_CURRENT_SELECTION" datatype="INT16">15</parameter>
        <parameter symbol="PARAM_SET_TEMPERATURE" datatype="FLOAT32">5.45</parameter>
        <parameter symbol="PARAM_ENABLE_FAST_MODE" datatype="BOOL">1</parameter>
    </parameters>
    <configuration initializeUndefinedRegisters="true" initialValue="0">
        ...
    </configuration>
    <simulation plcScanTime="1000">
        ...
    </simulation>
</device>
```

These parameters can be used like any other register on the simulation, but as **read-only variables**.

## How to change the parameters dynamically?
Specified parameters can be overwritten by command line arguments, by specifying the argument -e -env.

Example:
```
docker run -it -v $PWD/:/simulation -p5002:5002 paulorb/modbus-simulator-cli   -f /simulation/configuration-simulation.xml -e PARAM_CURRENT_SELECTION=10 -e PARAM_SET_TEMPERATURE=0.5
```

Please note the value specified as argument needs to match the parameter's datatype specified on the xml.