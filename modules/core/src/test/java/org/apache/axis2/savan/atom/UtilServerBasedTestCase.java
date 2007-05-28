/*
* Copyright 2004-2006 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.axis2.savan.atom;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;

public class UtilServerBasedTestCase extends TestCase {

    public UtilServerBasedTestCase() {
        super(UtilServerBasedTestCase.class.getName());
    }

    public UtilServerBasedTestCase(java.lang.String string) {
        super(string);
    }

    protected static Test getTestSetup(Test test) {
        return new TestSetup(test) {
            public void setUp() throws Exception {
                UtilServer.start();
            }

            public void tearDown() throws Exception {
                UtilServer.stop();
            }
        };
    }

    protected static Test getTestSetup2(Test test, final String param) {
        return new TestSetup(test) {
            public void setUp() throws Exception {
                UtilServer.start(param);
            }

            public void tearDown() throws Exception {
                UtilServer.stop();
            }
        };
    }

    protected static Test getTestSetup3(Test test, final String param1, final String param2) {
        return new TestSetup(test) {
            public void setUp() throws Exception {
                UtilServer.start(param1);
            }

            public void tearDown() throws Exception {
                UtilServer.stop();
            }
        };
    }
}
