package org.apache.axis2.engine;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URL;
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

public class WSDLClientTest extends UtilServerBasedTestCase implements TestConstants {

    protected AxisService service;

    public static Test suite() {
        return getTestSetup(new TestSuite(WSDLClientTest.class));
    }

    protected void setUp() throws Exception {
        service = AxisService.createService(Echo.class.getName(),
                UtilServer.getConfigurationContext().getAxisConfiguration(),RPCMessageReceiver.class );
        service.setName(serviceName.getLocalPart());
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testWSDLClient() throws AxisFault {
        try {
            URL wsdlURL = new URL("http://localhost:" + UtilServer.TESTING_PORT +
                    "/axis2/services/EchoXMLService?wsdl");
            ServiceClient serviceClient = new ServiceClient(null, wsdlURL,
                    new QName("http://ws.apache.org/axis2", "EchoXMLService"),
                    "EchoXMLServiceSOAP11port_http");
            OMElement payload = TestingUtils.createDummyOMElement("http://engine.axis2.apache.org/xsd");
            OMElement response = serviceClient.sendReceive(
                    new QName("http://engine.axis2.apache.org/xsd", "echoOM"), payload);
            assertNotNull(response);
            String textValue = response.getFirstElement().getFirstElement().getText();
            assertEquals(textValue, "Isaac Asimov, The Foundation Trilogy");
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

}
