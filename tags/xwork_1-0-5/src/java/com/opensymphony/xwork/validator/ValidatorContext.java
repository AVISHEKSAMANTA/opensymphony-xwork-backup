/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.validator;

import com.opensymphony.xwork.LocaleProvider;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.ValidationAware;


/**
 * The context for validation. This interface extends others to provide methods for reporting
 * errors and messages as well as looking up error messages in a resource bundle using a specific locale.
 *
 * @author Jason Carreira
 */
public interface ValidatorContext extends ValidationAware, TextProvider, LocaleProvider {

    /**
     * Translates a simple field name into a full field name in OGNL syntax.
     *
     * @param fieldName the field name to lookup.
     * @return the full field name in OGNL syntax.
     */
    String getFullFieldName(String fieldName);
}
