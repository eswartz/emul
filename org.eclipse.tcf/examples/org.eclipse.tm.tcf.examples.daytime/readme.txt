Readme for TCF Daytime Example
------------------------------

The Daytime Example shows how TCF/Java binding can be extended for a new, user defined service.
The example provides Java binding for DayTime service.
The example is mainly meant for developer's educational use,
DayTime service does not meant to be of any other value.

See "org.eclipse.tm.tcf.examples.daytime.agent" for details on extending TCF agent
with DayTime service implementation.

The example includes:
1. Definition of the service interface in Java: IDaytimeService.java
2. Implementation of IDaytimeService interface that translates
   interface method calls to TCF messages: DaytimeServiceProxy.java
3. Registration of the service using "org.eclipse.tm.tcf.startup" extension point, see plugin.xml