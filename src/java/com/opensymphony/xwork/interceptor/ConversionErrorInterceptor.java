/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.interceptor;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.opensymphony.xwork.util.XWorkConverter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * <!-- START SNIPPET: description -->
 * TODO: Give a description of the Interceptor.
 * <!-- END SNIPPET: description -->
 *
 * <!-- START SNIPPET: parameters -->
 * TODO: Describe the paramters for this Interceptor.
 * <!-- END SNIPPET: parameters -->
 *
 * <!-- START SNIPPET: extending -->
 * TODO: Discuss some possible extension of the Interceptor.
 * <!-- END SNIPPET: extending -->
 *
 * <pre>
 * <!-- START SNIPPET: example -->
 * &lt;!-- TODO: Describe how the Interceptor reference will effect execution --&gt;
 * &lt;action name="someAction" class="com.examples.SomeAction"&gt;
 *      TODO: fill in the interceptor reference.
 *     &lt;interceptor-ref name=""/&gt;
 *     &lt;result name="success"&gt;good_result.ftl&lt;/result&gt;
 * &lt;/action&gt;
 * <!-- END SNIPPET: example -->
 * </pre>
 *
 * ConversionErrorInterceptor adds conversion errors from the ActionContext to the Action's field errors
 *
 * @author Jason Carreira
 *         Date: Nov 27, 2003 3:57:06 PM
 */
public class ConversionErrorInterceptor extends AroundInterceptor {

    public static final String ORIGINAL_PROPERTY_OVERRIDE = "original.property.override";


    protected Object getOverrideExpr(ActionInvocation invocation, Object value) {
        return "'" + value + "'";
    }

    protected void after(ActionInvocation dispatcher, String result) throws Exception {
    }

    protected void before(ActionInvocation invocation) throws Exception {
        ActionContext invocationContext = invocation.getInvocationContext();
        Map conversionErrors = invocationContext.getConversionErrors();
        OgnlValueStack stack = invocationContext.getValueStack();

        HashMap fakie = null;

        for (Iterator iterator = conversionErrors.entrySet().iterator();
             iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String propertyName = (String) entry.getKey();
            Object value = entry.getValue();

            if (shouldAddError(propertyName, value)) {
                String message = XWorkConverter.getConversionErrorMessage(propertyName, stack);
                String addFieldErrorExpression = "addFieldError('" + propertyName + "','" + message + "')";
                stack.findValue(addFieldErrorExpression);

                if (fakie == null) {
                    fakie = new HashMap();
                }

                fakie.put(propertyName, getOverrideExpr(invocation, value));
            }
        }

        if (fakie != null) {
            // if there were some errors, put the original (fake) values in place right before the result
            stack.getContext().put(ORIGINAL_PROPERTY_OVERRIDE, fakie);
            invocation.addPreResultListener(new PreResultListener() {
                public void beforeResult(ActionInvocation invocation, String resultCode) {
                    Map fakie = (Map) invocation.getInvocationContext().get(ORIGINAL_PROPERTY_OVERRIDE);

                    if (fakie != null) {
                        invocation.getStack().setExprOverrides(fakie);
                    }
                }
            });
        }
    }

    protected boolean shouldAddError(String propertyName, Object value) {
        return true;
    }
}
