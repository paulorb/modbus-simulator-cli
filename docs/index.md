# Overview

## Why another Modbus Simulator?
Most of the options available are non-GPL, with limited customization options and 
focused on Windows. The idea is to develop an open-source solution available across
all platforms/OS.

## Features
* High performance TCP Server
* 100% Kotlin code - JVM (Multiplatform)
* Ability to associate memory registers with symbols (variable names)
* Ability to create custom simulation which can during runtime set, clear variables, execute math operations over registers, deal with specific timing requirements
* Available as CLI tool or as docker image