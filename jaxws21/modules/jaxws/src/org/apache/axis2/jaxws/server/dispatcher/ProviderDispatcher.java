/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.server.dispatcher;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.impl.alt.MethodMarshallerUtils;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.EndpointCallback;
import org.apache.axis2.jaxws.server.EndpointInvocationContext;
import org.apache.axis2.jaxws.server.ServerConstants;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.ExecutorFactory;
import org.apache.axis2.jaxws.utility.SingleThreadedExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * The ProviderDispatcher is used to invoke instances of a target endpoint that implement the {@link
 * javax.xml.ws.Provider} interface.
 * <p/>
 * The Provider<T> is a generic class, with certain restrictions on the parameterized type T.  This
 * implementation supports the following types:
 * <p/>
 * java.lang.String javax.activation.DataSource javax.xml.soap.SOAPMessage
 * javax.xml.transform.Source
 */
public class ProviderDispatcher extends JavaDispatcher {

    private static Log log = LogFactory.getLog(ProviderDispatcher.class);

    private BlockFactory _blockFactory = null;  // Cache the block factory
    private Class _providerType = null;        // Cache the provider type
    private Provider providerInstance = null;
    private Message message = null;
    private EndpointDescription endpointDesc = null;

    /**
     * Constructor
     *
     * @param _class
     * @param serviceInstance
     */
    public ProviderDispatcher(Class _class, Object serviceInstance) {
        super(_class, serviceInstance);
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.server.EndpointDispatcher#execute()
    */
    public MessageContext invoke(MessageContext request) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke javax.xml.ws.Provider based endpoint");
            log.debug("Invocation pattern: two way, sync");
        }

        initialize(request);

        providerInstance = getProviderInstance();

        Object param = createRequestParameters(request);

        if (log.isDebugEnabled()) {
            Class providerType = getProviderType();
            final Object input = providerType.cast(param);
            log.debug("Invoking Provider<" + providerType.getName() + ">");
            if (input != null) {
                log.debug("Parameter type: " + input.getClass().getName());
            }
            else {
                log.debug("Parameter is NULL");
            }
        }

        // Invoke the actual Provider.invoke() method
        boolean faultThrown = false;
        Throwable fault = null;
        Object[] input = new Object[] {param};
        Object responseParamValue = null;
        try {
            responseParamValue = invokeTargetOperation(getJavaMethod(), input);
        } catch (Throwable e) {
            fault = ClassUtils.getRootCause(e);
            faultThrown = true;
        }
        
        // Create the response MessageContext
        MessageContext responseMsgCtx = null;
        if (faultThrown) {
            // If a fault was thrown, we need to create a slightly different
            // MessageContext, than in the response path.
            responseMsgCtx = createFaultResponse(request, fault);
        } else {
            responseMsgCtx = createResponse(request, input, responseParamValue);
        }

        return responseMsgCtx;
    }
    
    public void invokeOneWay(MessageContext request) {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke javax.xml.ws.Provider based endpoint");
            log.debug("Invocation pattern: one way");
        }
        
        initialize(request);

        providerInstance = getProviderInstance();

        Object param = createRequestParameters(request);

        if (log.isDebugEnabled()) {
            Class providerType = getProviderType();
            final Object input = providerType.cast(param);
            log.debug("Invoking Provider<" + providerType.getName() + ">");
            if (input != null) {
                log.debug("Parameter type: " + input.getClass().getName());
            }
            else {
                log.debug("Parameter is NULL");
            }
        }

        ExecutorFactory ef = (ExecutorFactory) FactoryRegistry.getFactory(ExecutorFactory.class);
        Executor executor = ef.getExecutorInstance(ExecutorFactory.SERVER_EXECUTOR);
        
        // If the property has been set to disable thread switching, then we can 
        // do so by using a SingleThreadedExecutor instance to continue processing
        // work on the existing thread.
        Boolean disable = (Boolean) request.getProperty(ServerConstants.SERVER_DISABLE_THREAD_SWITCH);
        if (disable != null && disable.booleanValue()) {
            if (log.isDebugEnabled()) {
                log.debug("Server side thread switch disabled.  Setting Executor to the SingleThreadedExecutor.");
            }
            executor = new SingleThreadedExecutor();
        }
        
        Method m = getJavaMethod();
        Object[] params = new Object[] {param};
        
        EndpointInvocationContext eic = (EndpointInvocationContext) request.getInvocationContext();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        AsyncInvocationWorker worker = new AsyncInvocationWorker(m, params, cl, eic);
        FutureTask task = new FutureTask<AsyncInvocationWorker>(worker);
        executor.execute(task);
        
        return;
    }
    
    public void invokeAsync(MessageContext request, EndpointCallback callback) {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke javax.xml.ws.Provider based endpoint");
            log.debug("Invocation pattern: two way, async");
        }
        
        initialize(request);

        providerInstance = getProviderInstance();

        Object param = createRequestParameters(request);

        if (log.isDebugEnabled()) {
            Class providerType = getProviderType();
            final Object input = providerType.cast(param);
            log.debug("Invoking Provider<" + providerType.getName() + ">");
            if (input != null) {
                log.debug("Parameter type: " + input.getClass().getName());
            }
            else {
                log.debug("Parameter is NULL");
            }
        }

        ExecutorFactory ef = (ExecutorFactory) FactoryRegistry.getFactory(ExecutorFactory.class);
        Executor executor = ef.getExecutorInstance(ExecutorFactory.SERVER_EXECUTOR);
        
        // If the property has been set to disable thread switching, then we can 
        // do so by using a SingleThreadedExecutor instance to continue processing
        // work on the existing thread.
        Boolean disable = (Boolean) request.getProperty(ServerConstants.SERVER_DISABLE_THREAD_SWITCH);
        if (disable != null && disable.booleanValue()) {
            if (log.isDebugEnabled()) {
                log.debug("Server side thread switch disabled.  Setting Executor to the SingleThreadedExecutor.");
            }
            executor = new SingleThreadedExecutor();
        }
        
        Method m = getJavaMethod();
        Object[] params = new Object[] {param};
        
        EndpointInvocationContext eic = (EndpointInvocationContext) request.getInvocationContext();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        AsyncInvocationWorker worker = new AsyncInvocationWorker(m, params, cl, eic);
        FutureTask task = new FutureTask<AsyncInvocationWorker>(worker);
        executor.execute(task);
        
        return;
    }
    
    public Object createRequestParameters(MessageContext request) {
        // First we need to know what kind of Provider instance we're going
        // to be invoking against
        Class providerType = getProviderType();

        // REVIEW: This assumes there is only one endpoint description on the service.  Is that always the case?
        EndpointDescription endpointDesc = request.getEndpointDescription();

        // Now that we know what kind of Provider we have, we can create the 
        // right type of Block for the request parameter data
        Object requestParamValue = null;
        Message message = request.getMessage();
        if (message != null) {
            
            // Enable MTOM if indicated by the binding
            String bindingType = endpointDesc.getBindingType();
            if (bindingType != null) {
            	if (BindingUtils.isMTOMBinding(bindingType)) {
            		message.setMTOMEnabled(true);
            	}
            }

            // Save off the protocol info so we can use it when creating the response message.
            Protocol messageProtocol = message.getProtocol();
            // Determine what type blocks we want to create (String, Source, etc) based on Provider Type
            BlockFactory factory = createBlockFactory(providerType);


            Service.Mode providerServiceMode = endpointDesc.getServiceMode();

            if (providerServiceMode != null && providerServiceMode == Service.Mode.MESSAGE) {
                if (providerType.equals(SOAPMessage.class)) {
                    // We can get the SOAPMessage directly from the message itself
                    if (log.isDebugEnabled()) {
                        log.debug("Provider Type is SOAPMessage.");
                        log.debug("Number Message attachments=" + message.getAttachmentIDs().size());
                    }
                }

                requestParamValue = message.getValue(null, factory);
                if (requestParamValue == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "There are no elements to unmarshal.  ProviderDispatch will pass a null as input");
                    }
                }
            } else {
                // If it is not MESSAGE, then it is PAYLOAD (which is the default); only work with the body 
                Block block = message.getBodyBlock(null, factory);
                if (block != null) {
                    try {
                        requestParamValue = block.getBusinessObject(true);
                    } catch (WebServiceException e) {
                        throw ExceptionFactory.makeWebServiceException(e);
                    } catch (XMLStreamException e) {
                        throw ExceptionFactory.makeWebServiceException(e);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "No body blocks in SOAPMessage, Calling provider method with null input parameters");
                    }
                    requestParamValue = null;
                }
            }
        }
        
        
        return requestParamValue;
    }
    
    public MessageContext createResponse(MessageContext request, Object[] input, Object output) {
        if (log.isDebugEnabled()) {
            log.debug("Start createResponse");
        }
        Message m;
        EndpointDescription endpointDesc = null;
        try {
            endpointDesc = request.getEndpointDescription();
            Service.Mode mode = endpointDesc.getServiceMode();
            m = createMessageFromValue(output, request.getMessage().getProtocol(), mode);
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("Throwable caught");
                log.debug("Throwable=" + t);
            }
            throw ExceptionFactory.makeWebServiceException(t);
        }

        if (log.isDebugEnabled()) {
            log.debug("Response message is created.");
        }
        
        MessageContext response = null;
        try {
            // Enable MTOM if indicated by the binding
            String bindingType = endpointDesc.getBindingType();
            if (bindingType != null) {
            	if (BindingUtils.isMTOMBinding(bindingType)) {
                	m.setMTOMEnabled(true);
            	}
            }

            response = MessageContextUtils.createResponseMessageContext(request);
            response.setMessage(m);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Throwable caught creating Response MessageContext");
                log.debug("Throwable=" + e);
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("End createResponse");
            }
        }
        
        return response;
    }
    
    public MessageContext createFaultResponse(MessageContext request, Throwable fault) {
        if (log.isDebugEnabled()) {
            log.debug("Create XMLFault for createFaultResponse");
        }
        Message m;
        try {
            EndpointDescription endpointDesc = request.getEndpointDescription();
            Service.Mode mode = endpointDesc.getServiceMode();
            XMLFault xmlFault = MethodMarshallerUtils.createXMLFaultFromSystemException(fault);
            m = createMessageFromValue(xmlFault, request.getMessage().getProtocol(), mode);
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        MessageContext response = MessageContextUtils.createFaultMessageContext(request);
        response.setMessage(m);
        
        return response;
    }
    
    /**
     * Get the endpoint provider instance
     *
     * @return Provider
     * @throws Exception
     */
    public Provider getProvider() throws Exception {
        Provider p = getProviderInstance();
        setProvider(p);
        return p;
    }

    /**
     * Set the endpoint provider instance
     *
     * @param _provider
     */
    public void setProvider(Provider _provider) {
        this.providerInstance = _provider;
    }

    /**
     * Get the parameter for a given endpoint invocation
     *
     * @return
     * @throws Exception
     */
    public Message getMessage() throws Exception {
        return message;
    }

    /**
     * Set the parameter for a given endpoint invocation
     *
     * @param msg
     */
    public void setMessage(Message msg) {
        this.message = msg;
    }

    /*
    * Create a Message object out of the value object that was returned.
    */
    private Message createMessageFromValue(Object value, Protocol protocol, 
                                           Service.Mode mode) throws Exception {
        MessageFactory msgFactory =
                (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
        Message message = null;

        if (value != null) {
            Class providerType = getProviderType();
            BlockFactory factory = createBlockFactory(providerType);

            if (value instanceof XMLFault) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating message from XMLFault");
                }
                message = msgFactory.create(protocol);
                message.setXMLFault((XMLFault)value);
            } else if (mode != null && mode == Service.Mode.MESSAGE) {
                // For MESSAGE mode, work with the entire message, Headers and Body
                // This is based on logic in org.apache.axis2.jaxws.client.XMLDispatch.createMessageFromBundle()
                if (value instanceof SOAPMessage) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating message from SOAPMessage");
                    }
                    message = msgFactory.createFrom((SOAPMessage)value);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating message using " + factory);
                    }
                    Block block = factory.createFrom(value, null, null);
                    message = msgFactory.createFrom(block, null, protocol);
                }
            } else {
                // PAYLOAD mode deals only with the body of the message.
                if (log.isDebugEnabled()) {
                    log.debug("Creating message (payload) using " + factory);
                }
                Block block = factory.createFrom(value, null, null);
                message = msgFactory.create(protocol);
                message.setBodyBlock(block);
            }
        }

        if (message == null) {
            // If we didn't create a message above (because there was no value), create one here
            message = msgFactory.create(protocol);
        }


        return message;
    }

    /*
      * Determine the Provider type for this instance
      */
    private Provider getProviderInstance() {
        Class<?> clazz = getProviderType();

        if (!isValidProviderType(clazz)) {
            // TODO This will change once deployment code it in place
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("InvalidProvider", clazz.getName()));
        }

        Provider provider = null;
        if (clazz == String.class) {
            provider = (Provider<String>)serviceInstance;
        } else if (clazz == Source.class) {
            provider = (Provider<Source>)serviceInstance;
        } else if (clazz == SOAPMessage.class) {
            provider = (Provider<SOAPMessage>)serviceInstance;
        } else if (clazz == JAXBContext.class) {
            provider = (Provider<JAXBContext>)serviceInstance;
        }

        if (provider == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("InvalidProviderCreate", clazz.getName()));
        }

        return provider;

    }

    /*
     * Get the provider type from a given implemention class instance
     */
    private Class<?> getProviderType() {

        if (_providerType != null) {
            return _providerType;
        }
        Class providerType = null;

        Type[] giTypes = serviceImplClass.getGenericInterfaces();
        for (Type giType : giTypes) {
            ParameterizedType paramType = null;
            try {
                paramType = (ParameterizedType)giType;
            } catch (ClassCastException e) {
                // this may not be a parameterized interface
                continue;
            }
            Class interfaceName = (Class)paramType.getRawType();

            if (interfaceName == javax.xml.ws.Provider.class) {
                if (paramType.getActualTypeArguments().length > 1) {
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("pTypeErr"));
                }
                providerType = (Class)paramType.getActualTypeArguments()[0];
            }
        }
        _providerType = providerType;
        return providerType;
    }

    /*
    * Validate whether or not the Class passed in is a valid type of
    * javax.xml.ws.Provider<T>.  Per the JAX-WS 2.0 specification, the
    * parameterized type of a Provider can only be:
    *
    *   javax.xml.transform.Source
    *   javax.xml.soap.SOAPMessage
    *   javax.activation.DataSource
    *
    * We've also added support for String types which is NOT dictated
    * by the spec.
    */
    private boolean isValidProviderType(Class clazz) {
        boolean valid = clazz == String.class ||
                clazz == SOAPMessage.class ||
                clazz == Source.class ||
                clazz == DataSource.class;

        if (!valid) {
            if (log.isDebugEnabled()) {
                log.debug("Class " + clazz.getName() + " is not a valid Provider<T> type");
            }
        }

        return valid;
    }

    /*
    * Given a target class type for a payload, load the appropriate BlockFactory.
    */
    private BlockFactory createBlockFactory(Class type) {
        if (_blockFactory != null) {
            return _blockFactory;
        }

        if (type.equals(String.class)) {
            _blockFactory = (XMLStringBlockFactory)FactoryRegistry.getFactory(
                    XMLStringBlockFactory.class);
        } else if (type.equals(Source.class)) {
            _blockFactory = (SourceBlockFactory)FactoryRegistry.getFactory(
                    SourceBlockFactory.class);
        } else if (type.equals(SOAPMessage.class)) {
            _blockFactory = (SOAPEnvelopeBlockFactory)FactoryRegistry.getFactory(
                    SOAPEnvelopeBlockFactory.class);
        } else {
            throw ExceptionFactory.makeWebServiceException(
            		Messages.getMessage("bFactoryErr",type.getClass().getName()));
        }

        return _blockFactory;
    }
    
    protected Method getJavaMethod() {
        Method m = null;
        try {
            m = providerInstance.getClass().getMethod("invoke", new Class[] {getProviderType()});
        } catch (SecurityException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        } catch (NoSuchMethodException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        return m;
    }
    
    private void initialize(MessageContext mc) {

        mc.setOperationName(mc.getAxisMessageContext().getAxisOperation().getName());

        endpointDesc = mc.getEndpointDescription();
        String bindingType = endpointDesc.getBindingType();

        if (bindingType != null) {
            if (BindingUtils.isMTOMBinding(bindingType)) {
                mc.getMessage().setMTOMEnabled(true);
            }
        }

        //Set SOAP Operation Related properties in SOAPMessageContext.

        ContextUtils.addWSDLProperties_provider(mc);
    }


}