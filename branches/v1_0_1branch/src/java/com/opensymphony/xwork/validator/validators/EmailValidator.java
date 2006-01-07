/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.validator.validators;

import com.opensymphony.util.TextUtils;

import com.opensymphony.xwork.validator.ValidationException;


/**
 * EmailValidator checks that given field is a String and a valid email address.
 *
 * @author $Author$
 * @version $Revision$
 */
public class EmailValidator extends FieldValidatorSupport {
    //~ Methods ////////////////////////////////////////////////////////////////

    public void validate(Object object) throws ValidationException {
        String fieldName = getFieldName();
        Object value = this.getFieldValue(fieldName, object);

        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the field
        if (value == null) {
            return;
        }

        if (!(value instanceof String) || !TextUtils.verifyEmail((String) value)) {
            addFieldError(fieldName, object);
        }
    }
}