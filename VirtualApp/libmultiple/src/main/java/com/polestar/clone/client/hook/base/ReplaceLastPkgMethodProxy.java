package com.polestar.clone.client.hook.base;

import java.lang.reflect.Method;

import com.polestar.clone.client.hook.utils.MethodParameterUtils;

/**
 * @author Lody
 */

public class ReplaceLastPkgMethodProxy extends StaticMethodProxy {

	public ReplaceLastPkgMethodProxy(String name) {
		super(name);
	}

	@Override
	public boolean beforeCall(Object who, Method method, Object... args) {
		MethodParameterUtils.replaceLastAppPkg(args);
		return super.beforeCall(who, method, args);
	}
}
