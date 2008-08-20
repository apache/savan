Steps to build and run the Savan-Eventing sample
------------------------------------------------

First, you must set the AXIS2_HOME environment variable to point to a valid Axis2
installation (it should point to a directory which contains "repository/", "conf/",
etc.).

To build and deploy
-------------------

From this directory, run "ant deploy" to build the sample and automatically
copy the service archives, module archive, and supporting classes into your Axis2
installation.

Note that "ant" with no argument will simply build the sample, and "ant clean" will
remove the built artifacts and clean up.

To run
------

1. Make sure addressing module is inside $AXIS2_HOME/repository/modules
2. Start Axis2 server.
3. Run the 'samples.eventing.Client' class
   (you can pass the repository with a '-r' parameter and the server
    port with a '-p' parameter if you wish)

