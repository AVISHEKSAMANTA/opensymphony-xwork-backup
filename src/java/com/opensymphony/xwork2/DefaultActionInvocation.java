/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.InterceptorMapping;
import com.opensymphony.xwork2.config.entities.ResultConfig;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.PreResultListener;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.opensymphony.xwork2.util.profiling.UtilTimerStack;


/**
 * The Default ActionInvocation implementation
 *
 * @author Rainer Hermanns
 * @author tmjee
 * 
 * @version $Date$ $Id$
 * 
 * @see com.opensymphony.xwork2.DefaultActionProxy
 */
public class DefaultActionInvocation implements ActionInvocation {
    
	private static final long serialVersionUID = -585293628862447329L;

    //static {
    //    if (ObjectFactory.getContinuationPackage() != null) {
    //        continuationHandler = new ContinuationHandler();
    //    }
    //}
    private static final Logger LOG = LoggerFactory.getLogger(DefaultActionInvocation.class);

    protected Object action;
    protected ActionProxy proxy;
    protected List preResultListeners;
    protected Map extraContext;
    protected ActionContext invocationContext;
    protected Iterator interceptors;
    protected ValueStack stack;
    protected Result result;
    protected Result explicitResult;
    protected String resultCode;
    protected boolean executed = false;
    protected boolean pushAction = true;
    protected ObjectFactory objectFactory;
    protected ActionEventListener actionEventListener;
    protected ValueStackFactory valueStackFactory;
    protected Container container;
    protected UnknownHandler unknownHandler;

    public DefaultActionInvocation(final Map extraContext, final boolean pushAction) {
        DefaultActionInvocation.this.extraContext = extraContext;
        DefaultActionInvocation.this.pushAction = pushAction;
    }
    
    @Inject
    public void setValueStackFactory(ValueStackFactory fac) {
        this.valueStackFactory = fac;
    }
    
    @Inject
    public void setObjectFactory(ObjectFactory fac) {
        this.objectFactory = fac;
    }
    
    @Inject
    public void setContainer(Container cont) {
        this.container = cont;
    }
    
    @Inject(required=false)
    public void setUnknownHandler(UnknownHandler hand) {
        this.unknownHandler = hand;
    }
    
    @Inject(required=false)
    public void setActionEventListener(ActionEventListener listener) {
        this.actionEventListener = listener;
    }

    public Object getAction() {
        return action;
    }

    public boolean isExecuted() {
        return executed;
    }

    public ActionContext getInvocationContext() {
        return invocationContext;
    }

    public ActionProxy getProxy() {
        return proxy;
    }

    /**
     * If the DefaultActionInvocation has been executed before and the Result is an instance of ActionChainResult, this method
     * will walk down the chain of ActionChainResults until it finds a non-chain result, which will be returned. If the
     * DefaultActionInvocation's result has not been executed before, the Result instance will be created and populated with
     * the result params.
     *
     * @return a Result instance
     * @throws Exception
     */
    public Result getResult() throws Exception {
        Result returnResult = result;

        // If we've chained to other Actions, we need to find the last result
        while (returnResult instanceof ActionChainResult) {
            ActionProxy aProxy = ((ActionChainResult) returnResult).getProxy();

            if (aProxy != null) {
                Result proxyResult = aProxy.getInvocation().getResult();

                if ((proxyResult != null) && (aProxy.getExecuteResult())) {
                    returnResult = proxyResult;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return returnResult;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        if (isExecuted())
            throw new IllegalStateException("Result has already been executed.");

        this.resultCode = resultCode;
    }


    public ValueStack getStack() {
        return stack;
    }

    /**
     * Register a com.opensymphony.xwork2.interceptor.PreResultListener to be notified after the Action is executed and before the
     * Result is executed. The ActionInvocation implementation must guarantee that listeners will be called in the order
     * in which they are registered. Listener registration and execution does not need to be thread-safe.
     *
     * @param listener
     */
    public void addPreResultListener(PreResultListener listener) {
        if (preResultListeners == null) {
            preResultListeners = new ArrayList(1);
        }

        preResultListeners.add(listener);
    }

    public Result createResult() throws Exception {

    	if (explicitResult != null) {
    	    Result ret = explicitResult;
    	    explicitResult = null;;
    		return ret;
    	}
        ActionConfig config = proxy.getConfig();
        Map results = config.getResults();

        ResultConfig resultConfig = null;

        synchronized (config) {
            try {
                resultConfig = (ResultConfig) results.get(resultCode);
            } catch (NullPointerException e) {
            }
            if (resultConfig == null) {
                // If no result is found for the given resultCode, try to get a wildcard '*' match.
                resultConfig = (ResultConfig) results.get("*");
            }
        }

        if (resultConfig != null) {
            try {
                Result result = objectFactory.buildResult(resultConfig, invocationContext.getContextMap());
                return result;
            } catch (Exception e) {
                LOG.error("There was an exception while instantiating the result of type " + resultConfig.getClassName(), e);
                throw new XWorkException(e, resultConfig);
            }
        } else if (resultCode != null && !Action.NONE.equals(resultCode) && unknownHandler != null) {
            return unknownHandler.handleUnknownResult(invocationContext, proxy.getActionName(), proxy.getConfig(), resultCode);
        }
        return null;
    }

    /**
     * @throws ConfigurationException If no result can be found with the returned code
     */
    public String invoke() throws Exception {
    	String profileKey = "invoke: ";
    	try {
    		UtilTimerStack.push(profileKey);
    		
    		if (executed) {
    			throw new IllegalStateException("Action has already executed");
    		}

    		if (interceptors.hasNext()) {
    			final InterceptorMapping interceptor = (InterceptorMapping) interceptors.next();
    			UtilTimerStack.profile("interceptor: "+interceptor.getName(), 
    					new UtilTimerStack.ProfilingBlock<String>() {
							public String doProfiling() throws Exception {
				    			resultCode = interceptor.getInterceptor().intercept(DefaultActionInvocation.this);
				    			return null;
							}
    			});
    		} else {
    			resultCode = invokeActionOnly();
    		}

    		// this is needed because the result will be executed, then control will return to the Interceptor, which will
    		// return above and flow through again
    		if (!executed) {
    			if (preResultListeners != null) {
    				for (Iterator iterator = preResultListeners.iterator();
    					iterator.hasNext();) {
    					PreResultListener listener = (PreResultListener) iterator.next();
    					
    					String _profileKey="preResultListener: ";
    					try {
    						UtilTimerStack.push(_profileKey);
    						listener.beforeResult(this, resultCode);
    					}
    					finally {
    						UtilTimerStack.pop(_profileKey);
    					}
    				}
    			}

    			// now execute the result, if we're supposed to
    			if (proxy.getExecuteResult()) {
    				executeResult();
    			}

    			executed = true;
    		}

    		return resultCode;
    	}
    	finally {
    		UtilTimerStack.pop(profileKey);
    	}
    }

    public String invokeActionOnly() throws Exception {
    	return invokeAction(getAction(), proxy.getConfig());
    }

    protected void createAction(Map contextMap) {
        // load action
        String timerKey = "actionCreate: "+proxy.getActionName();
        try {
            UtilTimerStack.push(timerKey);
            action = objectFactory.buildAction(proxy.getActionName(), proxy.getNamespace(), proxy.getConfig(), contextMap);
        } catch (InstantiationException e) {
            throw new XWorkException("Unable to intantiate Action!", e, proxy.getConfig());
        } catch (IllegalAccessException e) {
            throw new XWorkException("Illegal access to constructor, is it public?", e, proxy.getConfig());
        } catch (Exception e) {
            String gripe = "";

            if (proxy == null) {
                gripe = "Whoa!  No ActionProxy instance found in current ActionInvocation.  This is bad ... very bad";
            } else if (proxy.getConfig() == null) {
                gripe = "Sheesh.  Where'd that ActionProxy get to?  I can't find it in the current ActionInvocation!?";
            } else if (proxy.getConfig().getClassName() == null) {
                gripe = "No Action defined for '" + proxy.getActionName() + "' in namespace '" + proxy.getNamespace() + "'";
            } else {
                gripe = "Unable to instantiate Action, " + proxy.getConfig().getClassName() + ",  defined for '" + proxy.getActionName() + "' in namespace '" + proxy.getNamespace() + "'";
            }

            gripe += (((" -- " + e.getMessage()) != null) ? e.getMessage() : " [no message in exception]");
            throw new XWorkException(gripe, e, proxy.getConfig());
        } finally {
            UtilTimerStack.pop(timerKey);
        }

        if (actionEventListener != null) {
            action = actionEventListener.prepare(action, stack);
        }
    }

    protected Map createContextMap() {
        Map contextMap;

        if ((extraContext != null) && (extraContext.containsKey(ActionContext.VALUE_STACK))) {
            // In case the ValueStack was passed in
            stack = (ValueStack) extraContext.get(ActionContext.VALUE_STACK);

            if (stack == null) {
                throw new IllegalStateException("There was a null Stack set into the extra params.");
            }

            contextMap = stack.getContext();
        } else {
            // create the value stack
            // this also adds the ValueStack to its context
            stack = valueStackFactory.createValueStack();

            // create the action context
            contextMap = stack.getContext();
        }

        // put extraContext in
        if (extraContext != null) {
            contextMap.putAll(extraContext);
        }

        //put this DefaultActionInvocation into the context map
        contextMap.put(ActionContext.ACTION_INVOCATION, this);
        contextMap.put(ActionContext.CONTAINER, container);

        return contextMap;
    }

    /**
     * Uses getResult to get the final Result and executes it
     * 
     * @throws ConfigurationException If not result can be found with the returned code
     */
    private void executeResult() throws Exception {
        result = createResult();

        String timerKey = "executeResult: "+getResultCode();
        try {
            UtilTimerStack.push(timerKey);
            if (result != null) {
                result.execute(this);
            } else if (resultCode != null && !Action.NONE.equals(resultCode)) {
                throw new ConfigurationException("No result defined for action " + getAction().getClass().getName() 
                        + " and result " + getResultCode(), proxy.getConfig());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No result returned for action "+getAction().getClass().getName()+" at "+proxy.getConfig().getLocation());
                }
            }
        } finally {
            UtilTimerStack.pop(timerKey);
        }
    }

    public void init(ActionProxy proxy) {
        this.proxy = proxy;
        Map contextMap = createContextMap();

        createAction(contextMap);

        if (pushAction) {
            stack.push(action);
            contextMap.put("action", action);
        }

        invocationContext = new ActionContext(contextMap);
        invocationContext.setName(proxy.getActionName());

        // get a new List so we don't get problems with the iterator if someone changes the list
        List interceptorList = new ArrayList(proxy.getConfig().getInterceptors());
        interceptors = interceptorList.iterator();
    }

    protected String invokeAction(Object action, ActionConfig actionConfig) throws Exception {
        String methodName = proxy.getMethod();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action method = " + actionConfig.getMethodName());
        }

        String timerKey = "invokeAction: "+proxy.getActionName();
        try {
            UtilTimerStack.push(timerKey);
            
            boolean methodCalled = false;
            Object methodResult = null;
            Method method = null;
            try {
                method = getAction().getClass().getMethod(methodName, new Class[0]);
            } catch (NoSuchMethodException e) {
                // hmm -- OK, try doXxx instead
                try {
                    String altMethodName = "do" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
                    method = getAction().getClass().getMethod(altMethodName, new Class[0]);
                } catch (NoSuchMethodException e1) {
                	// well, give the unknown handler a shot
                	if (unknownHandler != null) {
	                	try {
	                		methodResult = unknownHandler.handleUnknownActionMethod(action, methodName);
	                		methodCalled = true;
	                	} catch (NoSuchMethodException e2) {
	                		// throw the original one
	                		throw e;
	                	}
                	} else {
	            		throw e;
	            	}
                }
            }
        	
        	if (!methodCalled) {
        		methodResult = method.invoke(action, new Object[0]);
        	}
        	
            if (methodResult instanceof Result) {
            	this.explicitResult = (Result) methodResult;
            	return null;
            } else {
            	return (String) methodResult;
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The " + methodName + "() is not defined in action " + getAction().getClass() + "");
        } catch (InvocationTargetException e) {
            // We try to return the source exception.
            Throwable t = e.getTargetException();

            if (actionEventListener != null) {
                String result = actionEventListener.handleException(t, getStack());
                if (result != null) {
                    return result;
                }
            }
            if (t instanceof Exception) {
                throw(Exception) t;
            } else {
                throw e;
            }
        } finally {
            UtilTimerStack.pop(timerKey);
        }
    }
    
    
    
}
