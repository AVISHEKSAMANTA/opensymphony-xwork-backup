/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork;

import com.opensymphony.xwork.interceptor.ChainingInterceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;


/**
 * A special kind of view that invokes GenericDispatch (using the previously existing
 * ActionContext) and executes another action. This view takes one required parameter:
 * <ul>
 *  <li><b>actionName</b> - the name of the action that will be chained to</li>
 * </ul>
 *
 * @author $Author$
 * @version $Revision$
 */
public class ActionChainResult implements Result {
    
    //~ Static fields/initializers /////////////////////////////////////////////
    public static final String CHAIN_DEPTH = "com.opensymphony.xwork.ActionChainResult.CHAIN_DEPTH";

    // to prevent infinite recursion, only allow 100 chains within the same request
    public static int maxChainDepth = 100;

    private static final Log log = LogFactory.getLog(ActionChainResult.class);
    public static final String DEFAULT_PARAM = "actionName";

    //~ Instance fields ////////////////////////////////////////////////////////

    private ActionProxy proxy;
    private String actionName;

    /**
     * used to determine which namespace the Action is in that we're chaining
     * to.  if namespace is null, this defaults to the current namespace.
     */
    private String namespace;

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    /**
     * sets the namespace of the Action that we're chaining to.  if namespace
     * is null, this defaults to the current namespace.
     * @param namespace the name of the namespace we're chaining to
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public ActionProxy getProxy() {
        return proxy;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ActionChainResult)) {
            return false;
        }

        final ActionChainResult actionChainResult = (ActionChainResult) o;

        if ((actionName != null) ? (!actionName.equals(actionChainResult.actionName)) : (actionChainResult.actionName != null)) {
            return false;
        }

        return true;
    }

    /**
     * @param invocation the DefaultActionInvocation calling the action call stack
     */
    public void execute(ActionInvocation invocation) throws Exception {
        
        int currentDepth = currentChainDepth();
        if (currentDepth > maxChainDepth) {
            throw new XworkException("Chain infinite recursion detected, maxChainDepth=" + maxChainDepth);
        }
        incrementChainDepth();

        HashMap extraContext = new HashMap();
        extraContext.put(ActionContext.VALUE_STACK, ActionContext.getContext().getValueStack());
        extraContext.put(ActionContext.PARAMETERS, ActionContext.getContext().getParameters());
        extraContext.put("com.opensymphony.xwork.interceptor.component.ComponentManager", ActionContext.getContext().get("com.opensymphony.xwork.interceptor.component.ComponentManager"));
        extraContext.put(CHAIN_DEPTH, ActionContext.getContext().get(CHAIN_DEPTH));

        if (log.isDebugEnabled()) {
            log.debug("Chaining to action " + actionName);
        }

        // if the namespace wasn't explicitly defined, assume the current one
        if (this.namespace == null) {
            this.namespace = invocation.getProxy().getNamespace();
        }

        proxy = ActionProxyFactory.getFactory().createActionProxy(this.namespace, actionName, extraContext);
        proxy.execute();

    }

    public int hashCode() {
        return ((actionName != null) ? actionName.hashCode() : 0);
    }

    private int currentChainDepth() {
        Integer chainDepth = (Integer) ActionContext.getContext().get(CHAIN_DEPTH);

        if (chainDepth == null) {
            return 1;
        } else {
            return chainDepth.intValue();
        }
    }

    private void incrementChainDepth() {
        ActionContext.getContext().put(CHAIN_DEPTH, new Integer(currentChainDepth() + 1));
    }
}
