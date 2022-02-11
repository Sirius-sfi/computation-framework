# computation-framework
_computation-framework_ is an experimental framework for orchestrating time intensive computing operations by software means instead of using container frameworks.

## Structure
A system consists of two types of components:
1 _controller_ which administrates the workers and assigns tasks to them
1+ _workers_ which do the computational work

_computation-framework_ uses REST services for communication with the system and its components.

## Implementing a Use-Case
_computation-framework_ divides a computation request into 4 phases: validation, preparation, computation and result accumulation.

To realise a use-case one needs to implement an application based on this project's _implementation_ module (see _implementation-demo_ module for an easy example).
This use-case specific implementation defines what is happening on the _worker_ machine in each phase using the data from the computation request.

## Usage
The _controller_ needs to be started first, followed by one or more _workers_ configured to work with this controller.

The project contains Dockerfiles for the controller and the demo (the latter can be extended for one's own implementation).

### REST Calls
The following REST endpoints are available on the _controller_ for every use-case implementation.
We are using the _demo-implementation_'s domain in these examples.

#### status
http://localhost:8080/status/demo

Query the status of the computation request.


#### startComputation
http://localhost:8080/startComputation

Start a computation - for the _demo_ the request body looks like this:
```
{
    "domain": "DEMO",
    "payload": {
        "data": {
            "something": "lalala",
            "somethingElse": "lululu",
            "valid": "true",
            "calc": [
                {
                    "amount": "10",
                    "multiplier": "2"
                },
                {
                    "amount": "100",
                    "multiplier": "5"
                },
                {
                    "amount": "1024",
                    "multiplier": "3"
                }
            ]
        }
    }
}
```
The contents of _domain_ and the structure used inside the _data_ tag is use-case dependent.

#### result

http://localhost:8080/result/demo

Request the results of the computation.
