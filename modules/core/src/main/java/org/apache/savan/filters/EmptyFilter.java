/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.savan.filters;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.savan.SavanException;

/** This filter does not do any affective filtering. May be the default for some protocols. */
public class EmptyFilter implements Filter {

    public boolean checkCompliance(OMElement envelope) throws SavanException {
        return true;
    }

    public Object getFilterValue() {
        return null;
    }

    public void setUp(OMNode element) {
    }

}
