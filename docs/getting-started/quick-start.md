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
To be documented!