/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.util;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.XworkException;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.TypeConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;


/**
 * <!-- START SNIPPET: javadoc -->
 * <p/>
 * WebWork will automatically handle the most common type conversion for you. This includes support for converting to
 * and from Strings for each of the following:
 * <p/>
 * <ul>
 * <p/>
 * <li>String</li>
 * <p/>
 * <li>boolean / Boolean</li>
 * <p/>
 * <li>char / Character</li>
 * <p/>
 * <li>int / Integer, float / Float, long / Long, double / Double</li>
 * <p/>
 * <li>dates - uses the SHORT format for the Locale associated with the current request</li>
 * <p/>
 * <li>arrays - assuming the individual strings can be coverted to the individual items</li>
 * <p/>
 * <li>collections - if not object type can be determined, it is assumed to be a String and a new ArrayList is
 * created</li>
 * <p/>
 * </ul>
 * <p/>
 * <p/> Note that with arrays the type conversion will defer to the type of the array elements and try to convert each
 * item individually. As with any other type conversion, if the conversion can't be performed the standard type
 * conversion error reporting is used to indicate a problem occured while processing the type conversion.
 * <p/>
 * <!-- END SNIPPET: javadoc -->
 *
 * @author <a href="mailto:plightbo@gmail.com">Pat Lightbody</a>
 * @author Mike Mosiewicz
 * @author Rainer Hermanns
 */
public class XWorkBasicConverter extends DefaultTypeConverter {

    public Object convertValue(Map context, Object o, Member member, String s, Object value, Class toType) {
        Object result = null;

        if (value == null || toType.isAssignableFrom(value.getClass())) {
            // no need to convert at all, right?
            return value;
        }

        if (toType == String.class) {
            result = doConvertToString(context, value);
        } else if (toType == boolean.class) {
            result = doConvertToBoolean(value);
        } else if (toType == Boolean.class) {
            result = doConvertToBoolean(value);
        } else if (toType.isArray()) {
            result = doConvertToArray(context, o, member, s, value, toType);
        } else if (Date.class.isAssignableFrom(toType)) {
            result = doConvertToDate(context, value, toType);
        } else if (Collection.class.isAssignableFrom(toType)) {
            result = doConvertToCollection(context, o, member, s, value, toType);
        } else if (toType == Character.class) {
            result = doConvertToCharacter(value);
        } else if (toType == char.class) {
            result = doConvertToCharacter(value);
        } else if (Number.class.isAssignableFrom(toType)) {
            result = doConvertToNumber(context, value, toType);
        } else if (toType == Class.class) {
            result = doConvertToClass(value);
        }

        if (result == null) {
            if (value instanceof Object[]) {
                Object[] array = (Object[]) value;

                if (array.length >= 1) {
                    value = array[0];
                }

                // let's try to convert the first element only
                result = convertValue(context, o, member, s, value, toType);
            } else {
                result = super.convertValue(context, value, toType);
            }
            
            if (result == null && value != null && !"".equals(value)) {
                throw new XworkException("Cannot create type " + toType + " from value " + value);
            }
        }

        return result;
    }

    private Locale getLocale(Map context) {
        if (context == null) {
            return Locale.getDefault();
        }

        Locale locale = (Locale) context.get(ActionContext.LOCALE);

        if (locale == null) {
            locale = Locale.getDefault();
        }

        return locale;
    }

    /**
     * Creates a Collection of the specified type.
     *
     * @param fromObject
     * @param propertyName
     * @param toType       the type of Collection to create
     * @param memberType   the type of object elements in this collection must be
     * @param size         the initial size of the collection (ignored if 0 or less)
     * @return a Collection of the specified type
     */
    private Collection createCollection(Object fromObject, String propertyName, Class toType, Class memberType, int size) {
//        try {
//            Object original = Ognl.getValue(OgnlUtil.compile(propertyName),fromObject);
//            if (original instanceof Collection) {
//                Collection coll = (Collection) original;
//                coll.clear();
//                return coll;
//            }
//        } catch (Exception e) {
//            // fail back to creating a new one
//        }

        Collection result;

        if (toType == Set.class) {
            if (size > 0) {
                result = new HashSet(size);
            } else {
                result = new HashSet();
            }
        } else if (toType == SortedSet.class) {
            result = new TreeSet();
        } else {
            if (size > 0) {
                result = new XWorkList(memberType, size);
            } else {
                result = new XWorkList(memberType);
            }
        }

        return result;
    }

    private Object doConvertToArray(Map context, Object o, Member member, String s, Object value, Class toType) {
        Object result = null;
        Class componentType = toType.getComponentType();

        if (componentType != null) {
            TypeConverter converter = Ognl.getTypeConverter(context);

            if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                result = Array.newInstance(componentType, length);

                for (int i = 0; i < length; i++) {
                    Object valueItem = Array.get(value, i);
                    Array.set(result, i, converter.convertValue(context, o, member, s, valueItem, componentType));
                }
            } else {
                result = Array.newInstance(componentType, 1);
                Array.set(result, 0, converter.convertValue(context, o, member, s, value, componentType));
            }
        }

        return result;
    }

    private Object doConvertToCharacter(Object value) {
        if (value instanceof String) {
            String cStr = (String) value;

            return (cStr.length() > 0) ? new Character(cStr.charAt(0)) : null;
        }

        return null;
    }

    private Object doConvertToBoolean(Object value) {
        if (value instanceof String) {
            String bStr = (String) value;

            return Boolean.valueOf(bStr);
        }

        return null;
    }

    private Class doConvertToClass(Object value) {
        Class clazz = null;

        if (value instanceof String) {
            try {
                clazz = Class.forName((String) value);
            } catch (ClassNotFoundException e) {
                throw new XworkException(e.getLocalizedMessage(), e);
            }
        }

        return clazz;
    }

    private Collection doConvertToCollection(Map context, Object o, Member member, String prop, Object value, Class toType) {
        Collection result;
        Class memberType = String.class;

        if (o != null) {
            //memberType = (Class) XWorkConverter.getInstance().getConverter(o.getClass(), XWorkConverter.CONVERSION_COLLECTION_PREFIX + prop);
            memberType = XWorkConverter.getInstance().getObjectTypeDeterminer().getElementClass(o.getClass(), prop, null);

            if (memberType == null) {
                memberType = String.class;
            }
        }

        if (toType.isAssignableFrom(value.getClass())) {
            // no need to do anything
            result = (Collection) value;
        } else if (value.getClass().isArray()) {
            Object[] objArray = (Object[]) value;
            TypeConverter converter = Ognl.getTypeConverter(context);
            result = createCollection(o, prop, toType, memberType, objArray.length);

            for (int i = 0; i < objArray.length; i++) {
                result.add(converter.convertValue(context, o, member, prop, objArray[i], memberType));
            }
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            Collection col = (Collection) value;
            TypeConverter converter = Ognl.getTypeConverter(context);
            result = createCollection(o, prop, toType, memberType, col.size());

            for (Iterator it = col.iterator(); it.hasNext();) {
                result.add(converter.convertValue(context, o, member, prop, it.next(), memberType));
            }
        } else {
            result = createCollection(o, prop, toType, memberType, -1);
            result.add(value);
        }

        return result;
    }

    private Object doConvertToDate(Map context, Object value, Class toType) {
        Date result = null;

        if (value instanceof String) {
            String sa = (String) value;
            Locale locale = getLocale(context);

            DateFormat df =
                    java.sql.Time.class == toType ?
                            DateFormat.getTimeInstance(DateFormat.MEDIUM, locale) :
                            DateFormat.getDateInstance(DateFormat.SHORT, locale);

            try {
                result = df.parse(sa);
                if (! (Date.class == toType)) {
                    try {
                        Constructor constructor = toType.getConstructor(new Class[]{long.class});
                        return constructor.newInstance(new Object[]{new Long(result.getTime())});
                    } catch (Exception e) {
                        throw new XworkException("Couldn't create class " + toType + " using default (long) constructor", e);
                    }
                }
            } catch (ParseException e) {
                throw new XworkException("Could not parse date", e);
            }
        } else if (Date.class.isAssignableFrom(value.getClass())) {
            result = (Date) value;
        }

        return result;
    }

    private Object doConvertToNumber(Map context, Object value, Class toType) {
        if (value instanceof String) {
            if (toType == BigDecimal.class) {
                return new BigDecimal((String) value);
            } else if (toType == BigInteger.class) {
                return new BigInteger((String) value);
            } else {
                NumberFormat numFormat = NumberFormat.getInstance(getLocale(context));

                try {
                    // convert it to a Number
                    value = super.convertValue(context, numFormat.parse((String) value), toType);
                } catch (ParseException ex) {
                    throw new XworkException("Could not parse number", ex);
                }
            }
        } else if (value instanceof Object[]) {
            Object[] objArray = (Object[]) value;

            if (objArray.length == 1) {
                return doConvertToNumber(context, objArray[0], toType);
            }
        }

        // pass it through DefaultTypeConverter
        return super.convertValue(context, value, toType);
    }

    private String doConvertToString(Map context, Object value) {
        String result = null;

        if (value instanceof int[]) {
            int[] x = (int[]) value;
            List intArray = new ArrayList(x.length);

            for (int i = 0; i < x.length; i++) {
                intArray.add(new Integer(x[i]));
            }

            result = TextUtils.join(", ", intArray);
        } else if (value instanceof long[]) {
            long[] x = (long[]) value;
            List intArray = new ArrayList(x.length);

            for (int i = 0; i < x.length; i++) {
                intArray.add(new Long(x[i]));
            }

            result = TextUtils.join(", ", intArray);
        } else if (value instanceof double[]) {
            double[] x = (double[]) value;
            List intArray = new ArrayList(x.length);

            for (int i = 0; i < x.length; i++) {
                intArray.add(new Double(x[i]));
            }

            result = TextUtils.join(", ", intArray);
        } else if (value instanceof boolean[]) {
            boolean[] x = (boolean[]) value;
            List intArray = new ArrayList(x.length);

            for (int i = 0; i < x.length; i++) {
                intArray.add(new Boolean(x[i]));
            }

            result = TextUtils.join(", ", intArray);
        } else if (value instanceof Date) {
            DateFormat df = value instanceof java.sql.Time ?
                    DateFormat.getTimeInstance(DateFormat.MEDIUM, getLocale(context)) :
                    DateFormat.getDateInstance(DateFormat.SHORT, getLocale(context));
            result = df.format(value);
        } else if (value instanceof String[]) {
            result = TextUtils.join(", ", (String[]) value);
        }

        return result;
    }
}