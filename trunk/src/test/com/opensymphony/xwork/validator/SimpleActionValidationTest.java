/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.validator;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.ActionProxyFactory;
import com.opensymphony.xwork.ValidationAware;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.providers.MockConfigurationProvider;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * SimpleActionValidationTest
 *
 * Created : Jan 20, 2003 11:04:25 PM
 *
 * @author Jason Carreira
 */
public class SimpleActionValidationTest extends TestCase {
    //~ Methods ////////////////////////////////////////////////////////////////

    public void testAliasValidation() {
        HashMap params = new HashMap();
        params.put("baz", "10");

        //valid values
        params.put("bar", "7");
        params.put("date", "12/23/2002");

        HashMap extraContext = new HashMap();
        extraContext.put(ActionContext.PARAMETERS, params);

        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("", MockConfigurationProvider.VALIDATION_ACTION_NAME, extraContext);
            proxy.execute();

            ValidationAware validationAware = (ValidationAware) proxy.getAction();
            assertFalse(validationAware.hasFieldErrors());

            // put in an out-of-range value to see if the old validators still work
            params.put("bar", "42");
            proxy = ActionProxyFactory.getFactory().createActionProxy("", MockConfigurationProvider.VALIDATION_ALIAS_NAME, extraContext);
            proxy.execute();
            validationAware = (ValidationAware) proxy.getAction();
            assertTrue(validationAware.hasFieldErrors());

            Map errors = validationAware.getFieldErrors();
            assertTrue(errors.containsKey("baz"));

            List bazErrors = (List) errors.get("baz");
            assertEquals(1, bazErrors.size());

            String message = (String) bazErrors.get(0);
            assertEquals("baz out of range.", message);
            assertTrue(errors.containsKey("bar"));

            List barErrors = (List) errors.get("bar");
            assertEquals(1, barErrors.size());
            message = (String) barErrors.get(0);
            assertEquals("bar must be between 6 and 10, current value is 42.", message);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testMessageKey() {
        HashMap params = new HashMap();
        params.put("foo", "200");

        HashMap extraContext = new HashMap();
        extraContext.put(ActionContext.PARAMETERS, params);

        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("", MockConfigurationProvider.VALIDATION_ACTION_NAME, extraContext);
            proxy.execute();
            assertTrue(((ValidationAware) proxy.getAction()).hasFieldErrors());

            Map errors = ((ValidationAware) proxy.getAction()).getFieldErrors();
            List fooErrors = (List) errors.get("foo");
            assertEquals(1, fooErrors.size());

            String errorMessage = (String) fooErrors.get(0);
            assertNotNull(errorMessage);
            assertEquals("Foo Range Message", errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testParamterizedMessage() {
        ConfigurationManager.addConfigurationProvider(new MockConfigurationProvider());

        HashMap params = new HashMap();
        params.put("bar", "42");

        HashMap extraContext = new HashMap();
        extraContext.put(ActionContext.PARAMETERS, params);

        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("", MockConfigurationProvider.VALIDATION_ACTION_NAME, extraContext);
            proxy.execute();
            assertTrue(((ValidationAware) proxy.getAction()).hasFieldErrors());

            Map errors = ((ValidationAware) proxy.getAction()).getFieldErrors();
            List barErrors = (List) errors.get("bar");
            assertEquals(1, barErrors.size());

            String errorMessage = (String) barErrors.get(0);
            assertNotNull(errorMessage);
            assertEquals("bar must be between 6 and 10, current value is 42.", errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    protected void setUp() throws Exception {
        ConfigurationManager.clearConfigurationProviders();
        ConfigurationManager.addConfigurationProvider(new MockConfigurationProvider());
        ConfigurationManager.getConfiguration().reload();
        ActionContext.getContext().setLocale(Locale.US);
    }
}
