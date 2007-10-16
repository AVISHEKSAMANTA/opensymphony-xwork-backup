/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork2.validator;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.XWorkTestCase;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.validator.validators.URLValidator;

import junit.framework.TestCase;

/**
 * Test case for URLValidator
 * 
 * @author tm_jee
 * @version $Date$ $Id$
 */
public class URLValidatorTest extends XWorkTestCase {

	
	ValueStack stack;
	ActionContext actionContext;
	
	public void testAcceptNullValueForMutualExclusionOfValidators() throws Exception {
		
		URLValidator validator = new URLValidator();
		validator.setValidatorContext(new GenericValidatorContext(new Object()));
		validator.setFieldName("testingUrl1");
		validator.validate(new MyObject());
		
		assertFalse(validator.getValidatorContext().hasErrors());
		assertFalse(validator.getValidatorContext().hasActionErrors());
		assertFalse(validator.getValidatorContext().hasActionMessages());
		assertFalse(validator.getValidatorContext().hasFieldErrors());
	}
	
	public void testInvalidEmptyValue() throws Exception {
		
		URLValidator validator = new URLValidator();
		validator.setValidatorContext(new GenericValidatorContext(new Object()));
		validator.setFieldName("testingUrl2");
		validator.validate(new MyObject());
		
		assertFalse(validator.getValidatorContext().hasErrors());
		assertFalse(validator.getValidatorContext().hasActionErrors());
		assertFalse(validator.getValidatorContext().hasActionMessages());
		assertFalse(validator.getValidatorContext().hasFieldErrors());
	}
	
	public void testInvalidValue() throws Exception {
		
		URLValidator validator = new URLValidator();
		validator.setValidatorContext(new GenericValidatorContext(new Object()));
		validator.setFieldName("testingUrl3");
		validator.validate(new MyObject());
		
		assertTrue(validator.getValidatorContext().hasErrors());
		assertFalse(validator.getValidatorContext().hasActionErrors());
		assertFalse(validator.getValidatorContext().hasActionMessages());
		assertTrue(validator.getValidatorContext().hasFieldErrors());
	}
	
	
	public void testValidUrl1() throws Exception {
		
		URLValidator validator = new URLValidator();
		validator.setValidatorContext(new GenericValidatorContext(new Object()));
		validator.setFieldName("testingUrl4");
		validator.validate(new MyObject());
		
		assertFalse(validator.getValidatorContext().hasErrors());
		assertFalse(validator.getValidatorContext().hasActionErrors());
		assertFalse(validator.getValidatorContext().hasActionMessages());
		assertFalse(validator.getValidatorContext().hasFieldErrors());
	}
	
	public void testValidUrl2() throws Exception {
		
		URLValidator validator = new URLValidator();
		validator.setValidatorContext(new GenericValidatorContext(new Object()));
		validator.setFieldName("testingUrl5");
		validator.validate(new MyObject());
		
		assertFalse(validator.getValidatorContext().hasErrors());
		assertFalse(validator.getValidatorContext().hasActionErrors());
		assertFalse(validator.getValidatorContext().hasActionMessages());
		assertFalse(validator.getValidatorContext().hasFieldErrors());
	}
	
	protected void setUp() throws Exception {
	    super.setUp();
		stack = ActionContext.getContext().getValueStack();
		actionContext = ActionContext.getContext();
	}
	
	protected void tearDown() throws Exception {
	    super.tearDown();
		stack = null;
		actionContext = null;
	}
	
	
	class MyObject {
		public String getTestingUrl1() {
			return null;
		}
		
		public String getTestingUrl2() {
			return "";
		}
		
		public String getTestingUrl3() {
			return "sasdasd@asddd";
		}
		
		public String getTestingUrl4() {
			//return "http://yahoo.com/";
			return "http://www.jroller.com1?qwe=qwe";
		}
		
		public String getTestingUrl5() {
			return "http://yahoo.com/articles?id=123";
		}
	}
}
