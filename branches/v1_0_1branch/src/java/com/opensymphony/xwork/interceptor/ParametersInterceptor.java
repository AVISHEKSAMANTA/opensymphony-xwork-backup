/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.interceptor;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.InstantiatingNullHandler;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.opensymphony.xwork.util.XWorkConverter;
import com.opensymphony.xwork.util.XWorkMethodAccessor;

import java.util.Iterator;
import java.util.Map;


/**
 * @author $Author$
 * @version $Revision$
 */
public class ParametersInterceptor extends AroundInterceptor {
    //~ Methods ////////////////////////////////////////////////////////////////

    protected void after(ActionInvocation dispatcher, String result) throws Exception {
    }

    protected void before(ActionInvocation invocation) throws Exception {
        if (!(invocation.getAction() instanceof NoParameters)) {
            final Map parameters = ActionContext.getContext().getParameters();

            if (log.isDebugEnabled()) {
                log.debug("Setting params " + parameters);
            }

            try {
                invocation.getInvocationContext().put(InstantiatingNullHandler.CREATE_NULL_OBJECTS, Boolean.TRUE);
                invocation.getInvocationContext().put(XWorkMethodAccessor.DENY_METHOD_EXECUTION, Boolean.TRUE);
                invocation.getInvocationContext().put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.TRUE);

                if (parameters != null) {
                    final OgnlValueStack stack = ActionContext.getContext().getValueStack();

                    for (Iterator iterator = parameters.entrySet().iterator();
                            iterator.hasNext();) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        stack.setValue(entry.getKey().toString(), entry.getValue());
                    }
                }
            } finally {
                invocation.getInvocationContext().put(InstantiatingNullHandler.CREATE_NULL_OBJECTS, Boolean.FALSE);
                invocation.getInvocationContext().put(XWorkMethodAccessor.DENY_METHOD_EXECUTION, Boolean.FALSE);
                invocation.getInvocationContext().put(XWorkConverter.REPORT_CONVERSION_ERRORS, Boolean.FALSE);
            }
        }
    }
}