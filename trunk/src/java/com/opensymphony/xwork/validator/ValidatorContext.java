/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.validator;

import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.ValidationAware;
import com.opensymphony.xwork.LocaleProvider;


/**
 * ValidatorContext
 * @author Jason Carreira
 * Created Aug 3, 2003 12:30:32 AM
 */
public interface ValidatorContext extends ValidationAware, TextProvider, LocaleProvider {
}
