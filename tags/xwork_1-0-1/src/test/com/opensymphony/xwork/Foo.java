/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
/*
 * Created on Nov 11, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.opensymphony.xwork;


/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Foo {
    //~ Instance fields ////////////////////////////////////////////////////////

    String name = null;

    //~ Constructors ///////////////////////////////////////////////////////////

    public Foo() {
        name = "not set";
    }

    public Foo(String name) {
        this.name = name;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }
}