package com.polestar.clone.client.hook.base;

import java.lang.reflect.Method;

import com.polestar.clone.client.hook.utils.MethodParameterUtils;

/**
 * @author Lody
 */

public class ReplaceSequencePkgMethodProxy extends StaticMethodProxy {

	private int sequence;

	public ReplaceSequencePkgMethodProxy(String name, int sequence) {
		super(name);
		this.sequence = sequence;
	}

	@Override
	public boolean beforeCall(Object who, Method method, Object... args) {
		MethodParameterUtils.replaceSequenceAppPkg(args, sequence);
		return super.beforeCall(who, method, args);
	}
}
