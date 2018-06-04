package com.demodashi;

/**
 * 
 * @author ly
 *
 */
public interface UserService {
	
	String weixinPay(String userId, String productId) throws Exception;
}
