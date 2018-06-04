package com.demodashi.pay.util;

public class PayConfigUtil {
	//初始化
	public final static String APP_ID = "wx7fece4f7d89266ab"; //公众账号appid（改为自己实际的）
	public final static String MCH_ID = "1489224092"; //商户号（改为自己实际的）
	public final static String API_KEY = "Powerich1489224092abcdefghijklmn"; //（改为自己实际的）key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	//现在是自己设置了一个本机电脑项目到外网访问的地址
	//这地址可能不能回调，要用服务器上的地址就保证可以了。
	//企业向个人账号付款的URL
	//微信支付回调接口，就是微信那边收到（改为自己实际的）  现在这个地址并不能直接打开
	public final static String NOTIFY_URL = "http://jk.powerrich.com.cn/platform/WxpNotify";

	public final static String CREATE_IP = "183.240.212.232";//发起支付ip（改为自己实际的）


	//有关url
	//统一下单
	public final static String UFDODER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";


	public final static String APP_SECRET = "";
	public final static String SEND_EED_PACK_URL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";

}
