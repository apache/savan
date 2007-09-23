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

To deploy
---------
1. Run 'ant deploy' 
(This will copy sample services to ${axis2.home}/repository/services & savan module into ${axis2.home}/repository/modules)

To run
------
1. Make sure addressing module is inside ${axis2.home}/repository/modules
2. Start Axis2 server.
3. Run the 'samples.eventing.Client' class, you can pass the repository with a '-r' parameter and the server port with a '-p' parameter.
