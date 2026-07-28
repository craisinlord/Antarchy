package com.craisinlord.antarchy.content.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class AlphaVertexConsumerProxy {
    private AlphaVertexConsumerProxy() {
    }

    public static VertexConsumer wrap(VertexConsumer delegate, float alphaScale) {
        if (alphaScale >= 0.999F) {
            return delegate;
        }

        final VertexConsumer[] proxyHolder = new VertexConsumer[1];
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object[] forwardedArgs = args;
                if ("setColor".equals(method.getName()) && args != null && args.length == 4 && args[3] instanceof Integer alpha) {
                    forwardedArgs = args.clone();
                    forwardedArgs[3] = Math.max(0, Math.min(255, Math.round(alpha * alphaScale)));
                }

                Object result = method.invoke(delegate, forwardedArgs);
                return result == delegate ? proxyHolder[0] : result;
            }
        };

        proxyHolder[0] = (VertexConsumer) Proxy.newProxyInstance(
                VertexConsumer.class.getClassLoader(),
                new Class<?>[] { VertexConsumer.class },
                handler
        );
        return proxyHolder[0];
    }
}
