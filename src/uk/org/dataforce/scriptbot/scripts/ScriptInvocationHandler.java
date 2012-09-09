/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.org.dataforce.scriptbot.scripts;

import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 *
 * @author shane
 */
public class ScriptInvocationHandler implements InvocationHandler {
    /** Class we are pretending to be. */
    final Class myClass;

    /** Server Bridge that owns us. */
    private final ParserBridge myBridge;

    /**
     * Create a new ScriptInvocationHandler.
     */
    private ScriptInvocationHandler(final Class clazz, final ParserBridge bridge) {
        myClass = clazz;
        myBridge = bridge;
    }

    /**
     * Get a CallbackInterface Proxy that implements this.
     */
    public static CallbackInterface getProxy(final Class clazz, final ParserBridge bridge) {
        return (CallbackInterface)Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{clazz}, new ScriptInvocationHandler(clazz, bridge));
    }

    /** {@inheritDoc} */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class)  {
            String methodName = method.getName();
            if (methodName.equals("hashCode"))  {
                return this.hashCode();
            } else if (methodName.equals("equals")) {
                return this.equals(args[0]);
            } else if (methodName.equals("toString")) {
                return this.toString();
            }
        }
        myBridge.getServer().getScriptHandler().callBound(method.getName(), args);
        return null;
    }
}