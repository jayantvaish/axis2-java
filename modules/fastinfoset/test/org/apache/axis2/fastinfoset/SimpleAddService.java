/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.fastinfoset;

/**
 * @author Sanjaya Karunasena (sanjayak@yahoo.com)
 * @date Feb 16, 2007
 */
public class SimpleAddService {
	
	public int addInts(int val1, int val2) {
		System.out.println("Received " + val1 + " & " + val2);
		return val1 + val2;
	}

	public float addFloats(float val1, float val2) {
		System.out.println("Received " + val1 + " & " + val2);
		return val1 + val2;
	}
	
	public String addStrings(String val1, String val2) {
		System.out.println("Received " + val1 + " & " + val2);
		return val1 + val2;
	}
}
