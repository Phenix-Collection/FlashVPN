package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

/**
 * Class: Created by andy on 16-8-3. TODO:
 */
class StartVoiceActivity extends StartActivity {
	@Override
	public String getName() {
		return "startVoiceActivity";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return super.call(who, method, args);
	}
}
