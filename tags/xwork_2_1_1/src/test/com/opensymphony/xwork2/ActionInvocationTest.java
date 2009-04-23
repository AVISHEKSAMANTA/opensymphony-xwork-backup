/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork2;

import java.util.HashMap;

import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.providers.XmlConfigurationProvider;


/**
 * @author $Author$
 * @version $Revision$
 */
public class ActionInvocationTest extends XWorkTestCase {

    public void testCommandInvocation() throws Exception {
        ActionProxy baseActionProxy = actionProxyFactory.createActionProxy(
                "baz", "commandTest", null, null);
        assertEquals("success", baseActionProxy.execute());

        ActionProxy commandActionProxy = actionProxyFactory.createActionProxy(
                "baz", "myCommand", null, null);
        assertEquals(SimpleAction.COMMAND_RETURN_CODE, commandActionProxy.execute());
    }
    
    public void testCommandInvocationDoMethod() throws Exception {
        ActionProxy baseActionProxy = actionProxyFactory.createActionProxy(
                "baz", "doMethodTest", null, null);
        assertEquals("input", baseActionProxy.execute());
    }
    
    public void testCommandInvocationUnknownHandler() throws Exception {
    	
        DefaultActionProxy baseActionProxy = (DefaultActionProxy) actionProxyFactory.createActionProxy(
                "baz", "unknownMethodTest", "unknownmethod", null);
        ((DefaultActionInvocation)baseActionProxy.getInvocation()).unknownHandler = new UnknownHandler() {
			public ActionConfig handleUnknownAction(String namespace, String actionName) throws XWorkException { return null;}
			public Result handleUnknownResult(ActionContext actionContext, String actionName, ActionConfig actionConfig, String resultCode) throws XWorkException {
				return null;
			}
			public Object handleUnknownActionMethod(Object action, String methodName) throws NoSuchMethodException {
				if (methodName.equals("unknownmethod")) {
					return "found";
				} else {
					return null;
				}
			}
        };
        assertEquals("found", baseActionProxy.execute());
    }
    
    public void testResultReturnInvocation() throws Exception {
        ActionProxy baseActionProxy = actionProxyFactory.createActionProxy(
                "baz", "resultAction", null, null);
        assertEquals(null, baseActionProxy.execute());
        assertTrue(SimpleAction.resultCalled);
    }

    public void testSimple() {
        HashMap params = new HashMap();
        params.put("blah", "this is blah");

        HashMap extraContext = new HashMap();
        extraContext.put(ActionContext.PARAMETERS, params);

        try {
            ActionProxy proxy = actionProxyFactory.createActionProxy( "", "Foo", null, extraContext);
            proxy.execute();
            assertEquals("this is blah", proxy.getInvocation().getStack().findValue("[1].blah"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        // ensure we're using the default configuration, not simple config
        loadConfigurationProviders(new XmlConfigurationProvider("xwork-sample.xml"));
    }
}