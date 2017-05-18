package com.lucascauthen.ArduinoMazo.utility;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class NullObject {

    private static class NullInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    public static <T> T create(Class<T> nullObjClazz) {
        Object proxy = Proxy.newProxyInstance(nullObjClazz.getClassLoader(), new Class<?>[]{nullObjClazz}, new NullInvocationHandler());
        return nullObjClazz.cast(proxy);
    }
}