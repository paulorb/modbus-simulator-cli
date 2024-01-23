# Quick Start

The most simple and quick way to run the modbus simulator is by using docker,
in this example we will run the modbus simulator without simulation.

1. Pull the Modbus Simulator

```
docker pull paulorb/modbus-simulator-cli
```

2. Run the Modbus Simulator

```
docker run --rm -p 5002:5002 paulorb/modbus-simulator-cli
```

This simulator offers many other types of simulation, **predefined ones** or **custom** (based on a configuration file)

## Predefined Simulations

### Random values
By specifying **-sr** argument the simulator will generate random numbers for register values, each time a read operation
is performed, write operations are ignored.


## Custom Simulation
The simulation can be customized by providing a configuration (in XML format) to the simulator. This is an example of configuration:

```xml
<?xml version="1.0" encoding="US-ASCII" ?>
<device ip="0.0.0.0" port="502">
    <configuration initializeUndefinedRegisters="true" initialValue="0">
        <registers>
            <register addressType="HOLDING_REGISTER" address="14" symbol="RPM_MOTOR">500</register>
            <register addressType="HOLDING_REGISTER" address="10" datatype="FLOAT32" symbol="TEMPERATURE">-12.5</register>
            <register addressType="HOLDING_REGISTER" address="0"  datatype="INT16" symbol="RPM_MOTOR1">1</register>
            <register addressType="COIL" address="1" symbol="RELAYON">1</register>
            <register addressType="DISCRETE_INPUT" address="0" symbol="RELAY_STATUS">1</register>
            <register addressType="INPUT_REGISTER" address="0" symbol="RPM">1</register>
        </registers>
    </configuration>
    <simulation plcScanTime="1000">
        <set symbol="TEMPERATURE">2.0</set>
        <set symbol="RPM_MOTOR">15</set>
        <delay>500</delay>
        <set symbol="RPM">1</set>
        <delay>500</delay>
        <set symbol="RPM">0</set>
        <delay>500</delay>
        <sub symbol="RPM_MOTOR">10</sub>
        <delay>500</delay>
        <add symbol="RPM_MOTOR">10</add>
    </simulation>
</device>
```

Sections **device** and  **configuration** are **mandatory** 

## Custom simulation with docker
For the custom simulation to work, the configuration file must be defined. 

1. Place the configuration file in a folder for example *simulation* folder
2. From inside the folder (cd simulation) execute the following command 

```
docker run -it -v $PWD/:/simulation -p5002:5002 paulorb/modbus-simulator-cli   -f /simulation/simulation.xml
```


## Device section
Device section is used to configure the network parameters of the driver
*TODO: define device section*
