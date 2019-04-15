package com.kt.james.beplugincore.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * author: James
 * 2019/4/11 16:59
 * version: 1.0
 */
public class ReflectInvoker {

    public static Object invokeMethod(Object target, String className, String methodName,
                                      Class[] paramTypes, Object[] paramValues) {
        try {
            Class clazz = Class.forName(className);
            return invokeMethod(target, clazz, methodName, paramTypes, paramValues);
        } catch (ClassNotFoundException e) {
            LogUtil.printException("ClassNotFoundException", e);
        }
        return null;
    }

    public static Object invokeMethod(Object target, Class clazz, String methodName,
                                      Class[] paramTypes, Object[] paramValues) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(target, paramValues);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getField(Object target, Class clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(target);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getField(Object target, String className, String fieldName) {
        try {
            Class clazz = Class.forName(className);
            return getField(target, clazz, fieldName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setField(Object target, String className, String fieldName, Object field) {
        try {
            Class clazz = Class.forName(className);
            setField(target, clazz, fieldName, field);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setField(Object target, Class clazz, String fieldName, Object fieldVal) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(target, fieldVal);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
