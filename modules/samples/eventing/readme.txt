Steps to build and run the Savan-Eventing sample
------------------------------------------------

To build
--------
1. Open build.xml
2. Change value of 'axis2.home' property to a valid Axis2 home
3. Run 'ant' to build the sample.

To clean
--------
1. Run 'ant clean'


To run
------
1. Start a Axis2 server.
2. Deploy savan and addressing modules.
3. Deploy the three services that were created in the 'build' folder.
4. Run the 'samples.eventing.Client' class, you can pass the repository with a '-r' parameter and the server port with a '-p' parameter.
