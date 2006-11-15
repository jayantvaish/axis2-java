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

package test;

import junit.framework.TestCase;
import test.stub.WSDLInteropTestDocLitParametersSoapStub;
import test.stub.databinding.org.soapinterop.*;

import java.rmi.RemoteException;

public class AspNetRound3DoclitparamInteropTest extends TestCase{

    WSDLInteropTestDocLitParametersSoapStub stub = null;
    EchoStringDocument strParaDoc = null;
    EchoStringDocument.EchoString echoStr = null;
    String str = "Gayan Asanka";
    EchoStringResponseDocument retStrDoc = null;
    EchoStringArrayDocument strArrayParaDoc = null;
    EchoStringArrayDocument.EchoStringArray echoStrArray = null;
    ArrayOfString strLitArr = null;
    EchoStringArrayResponseDocument retArrayDoc = null;
    String[] strArry = {"String 1", "String 2", "String 3"};
    ArrayOfString retArray = null;
    EchoStructDocument structParaDoc = null;
    SOAPStruct soapStruct = null;
    float flt = (float)1234.456;
    int i = 123456;
    EchoStructDocument.EchoStruct echoStruct = null;
    EchoStructResponseDocument retStructDoc = null;
    SOAPStruct retStruct = null;


    public void setUp() throws Exception {
        stub = new WSDLInteropTestDocLitParametersSoapStub();
    }

    public void testEchoString() throws RemoteException {
        echoStr= EchoStringDocument.EchoString.Factory.newInstance();
        echoStr.setParam0(str);
        strParaDoc = EchoStringDocument.Factory.newInstance();
        strParaDoc.setEchoString(echoStr);
        retStrDoc = stub.echoString(strParaDoc);
        assertEquals(str,retStrDoc.getEchoStringResponse().getReturn());
    }

    public void testEchoStringArray() throws Exception {
        strLitArr = ArrayOfString.Factory.newInstance();
        strLitArr.setStringArray(strArry);
        echoStrArray = EchoStringArrayDocument.EchoStringArray.Factory.newInstance();
        echoStrArray.setParam0(strLitArr);
        strArrayParaDoc = EchoStringArrayDocument.Factory.newInstance();
        strArrayParaDoc.setEchoStringArray1(echoStrArray);
        retArrayDoc = stub.echoStringArray(strArrayParaDoc);
        retArray=retArrayDoc.getEchoStringArrayResponse().getReturn();
        assertEquals(strArry[0],retArray.getStringArray()[0]);
        assertEquals(strArry[1],retArray.getStringArray()[1]);
        assertEquals(strArry[2],retArray.getStringArray()[2]);
    }

    public void testEchoStruct() throws Exception {
        soapStruct = SOAPStruct.Factory.newInstance();
        soapStruct.setVarFloat(flt);
        soapStruct.setVarInt(i);
        soapStruct.setVarString(str);
        echoStruct = EchoStructDocument.EchoStruct.Factory.newInstance();
        echoStruct.setParam0(soapStruct);
        structParaDoc = EchoStructDocument.Factory.newInstance();
        structParaDoc.setEchoStruct(echoStruct);
        retStructDoc = stub.echoStruct(structParaDoc);
        retStruct = retStructDoc.addNewEchoStructResponse().getReturn();
        assertEquals(flt,retStruct.getVarFloat(),0);
        assertEquals(i,retStruct.getVarInt());
        assertEquals(str,retStruct.getVarString());

    }

}
