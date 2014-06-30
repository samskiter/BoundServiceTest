BoundServiceTest
================

A test of using app context and non-ui fragments to maintain a connection bound service.

This demonstrates making requests to a bound service to do work and using a retained, non-ui fragment to maintain the connection to the service.

The service implements a priority queue and the example also has a singleton, ModelObject, that binds and unbinds from the service to do longer, lower priority operations.

The service can therefore provide a central point to do e.g. network IO.
