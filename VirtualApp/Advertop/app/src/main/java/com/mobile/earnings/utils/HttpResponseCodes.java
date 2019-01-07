package com.mobile.earnings.utils;



public final class HttpResponseCodes{
	public static final int HTTP_SUCCESS                  = 200;
	public static final int HTTP_REFERRAL_CODE_NOT_EXISTS = 400;
	public static final int HTTP_USER_NOT_FOUND           = 401;
	public static final int HTTP_NOT_ENOUGH_FUNDS         = 402;
	public static final int HTTP_TASK_ALREADY_DONE        = 403;
	public static final int HTTP_APP_NOT_FOUND            = 404;
	public static final int HTTP_TASK_NOT_AVAILABLE       = 405;
	public static final int HTTP_AMOUNT_UNDER_LIMIT       = 406;
	public static final int HTTP_TASK_FAILED              = 410;
	public static final int HTTP_EMAIL_EXISTS             = 422;
	public static final int HTTP_PRIVAT_ERROR             = 423;
	public static final int HTTP_REQUEST_LIMIT_ERROR      = 429;
	public static final int HTTP_USER_ALREADY_EXISTS      = 500;
}
