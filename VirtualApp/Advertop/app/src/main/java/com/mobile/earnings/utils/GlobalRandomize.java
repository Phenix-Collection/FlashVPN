package com.mobile.earnings.utils;

import java.util.Random;



public class GlobalRandomize{

	public static int randomize(){
		Random random = new Random();
		return random.nextInt(2);
	}
}
