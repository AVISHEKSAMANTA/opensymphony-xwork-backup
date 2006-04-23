/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.util;

import com.opensymphony.xwork.util.CompoundRoot;

import ognl.*;

import java.beans.IntrospectionException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 *
 * @author $Author$
 * @version $Revision$
 */
public class CompoundRootAccessor implements PropertyAccessor, MethodAccessor, ClassResolver {
    //~ Methods ////////////////////////////////////////////////////////////////

    public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
        CompoundRoot root = (CompoundRoot) target;
        OgnlContext ognlContext = (OgnlContext) context;

        for (Iterator iterator = root.iterator(); iterator.hasNext();) {
            Object o = iterator.next();

            try {
                if (OgnlRuntime.hasSetProperty(ognlContext, o, name)) {
                    OgnlUtil.setProperty((String) name, value, o, context);
                }
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
    }

    public Object getProperty(Map context, Object target, Object name) throws OgnlException {
        CompoundRoot root = (CompoundRoot) target;
        OgnlContext ognlContext = (OgnlContext) context;

        if (name instanceof Integer) {
            Integer index = (Integer) name;

            return root.cutStack(index.intValue());
        } else if (name instanceof String) {
            for (Iterator iterator = root.iterator(); iterator.hasNext();) {
                Object o = iterator.next();

                try {
                    Object value = OgnlRuntime.getProperty(ognlContext, o, name);

                    //Ognl.getValue(OgnlUtil.compile((String) name), context, o);
                    if (value != null) {
                        ognlContext.pushEvaluation(new Evaluation(ognlContext.getCurrentEvaluation().getNode(), o));

                        return value;
                    }
                } catch (OgnlException e) {
                    // try the next one
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public Object callMethod(Map context, Object target, String name, List list) throws MethodFailedException {
        CompoundRoot root = (CompoundRoot) target;

        for (Iterator iterator = root.iterator(); iterator.hasNext();) {
            Object o = iterator.next();

            try {
                Object value = OgnlRuntime.callMethod((OgnlContext) context, o, name, name, list);

                if (value != null) {
                    return value;
                }
            } catch (OgnlException e) {
                // try the next one
            }
        }

        return null;
    }

    public Object callStaticMethod(Map transientVars, Class aClass, String s, List list) throws MethodFailedException {
        return null;
    }

    public Class classForName(String className, Map context) throws ClassNotFoundException {
        Object root = Ognl.getRoot(context);

        try {
            if (root instanceof CompoundRoot) {
                if (className.startsWith("vs")) {
                    CompoundRoot compoundRoot = (CompoundRoot) root;

                    if (className.equals("vs")) {
                        return compoundRoot.peek().getClass();
                    }

                    int index = Integer.parseInt(className.substring(2));

                    return compoundRoot.get(index - 1).getClass();
                }
            }
        } catch (Exception e) {
            // just try the old fashioned way
        }

        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }
}