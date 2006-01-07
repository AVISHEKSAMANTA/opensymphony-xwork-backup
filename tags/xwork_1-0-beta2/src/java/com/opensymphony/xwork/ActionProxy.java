/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork;

import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.util.OgnlValueStack;

import java.io.Serializable;


/**
 * ActionProxy
 * @author Jason Carreira
 * Created Jun 9, 2003 11:27:55 AM
 */
public interface ActionProxy extends Serializable {
    //~ Methods ////////////////////////////////////////////////////////////////

    Action getAction();

    String getActionName();

    ActionConfig getConfig();

    void setExecuteResult(boolean executeResult);

    boolean getExecuteResult();

    ActionInvocation getInvocation();

    String getNamespace();

    String execute() throws Exception;
}