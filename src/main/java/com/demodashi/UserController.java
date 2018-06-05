package com.demodashi;

import com.demodashi.pay.util.PayConfigUtil;
import com.demodashi.pay.util.PayToolUtil;
import com.demodashi.pay.util.QRUtil;
import com.demodashi.pay.util.XMLUtil4jdom;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.jdom.JDOMException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * demo 测试的例子
 *
 * @author ly
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    private static int numJi = 1;

    @Inject
    UserService userApplication;

    @ResponseBody
    @RequestMapping("/qrcode")
    public void qrcode(HttpServletRequest request, HttpServletResponse response,
                       ModelMap modelMap) {
        try {
            String productId = request.getParameter("productId");
            String userId = "user01";
            String text = userApplication.weixinPay(userId, productId);

            int width = 300;
            int height = 300;
            //二维码的图片格式
            String format = "gif";
            Hashtable hints = new Hashtable();
            //内容所使用编码
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix;
            try {
                bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
                //生成二维码
                QRUtil.writeToStream(bitMatrix, format, response.getOutputStream());
            } catch (WriterException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
        }
    }

    @ResponseBody
    @RequestMapping("/hadPay")
    public Map<String, Object> hadPay(UserVO user, HttpServletRequest request, HttpServletResponse response,
                                      ModelMap modelMap) {
        try {
            //简单的业务逻辑：在微信的回调接口里面，已经定义了，回调返回成功的话，那么 _PAY_RESULT 不为空
            if (request.getSession().getAttribute("_PAY_RESULT") != null) {
                return success("支付成功！");
            }
            String pay_result = request.getAttribute("PAY_RESULT").toString();
            System.out.println(pay_result);
            if(pay_result.equals("OK")){
                return success("支付成功！");
            }
            return error("没成功");
        } catch (Exception e) {
            return error(e);
        }
    }

    /**
     * 微信平台发起的回调方法，
     * 调用我们这个系统的这个方法接口，将扫描支付的处理结果告知我们系统
     *
     * @throws JDOMException
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/weixinNotify")
    public void weixinNotify(HttpServletRequest request, HttpServletResponse response) throws JDOMException, Exception {
        //读取参数  
        InputStream inputStream;
        StringBuffer sb = new StringBuffer();
        inputStream = request.getInputStream();
        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        inputStream.close();

        //解析xml成map
        Map<String, String> m = new HashMap<String, String>();
        m = XMLUtil4jdom.doXMLParse(sb.toString());

        //过滤空 设置 TreeMap  
        SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            String parameter = (String) it.next();
            String parameterValue = m.get(parameter);

            String v = "";
            if (null != parameterValue) {
                v = parameterValue.trim();
            }
            packageParams.put(parameter, v);
        }

        // 微信支付的API密钥
        String key = PayConfigUtil.API_KEY; //key
        //微信支付返回来的参数
        System.out.println(packageParams);
        //判断签名是否正确
        if (PayToolUtil.isTenpaySign("UTF-8", packageParams, key)) {
            //------------------------------  
            //处理业务开始  
            //------------------------------  
            String resXml = "";
            if ("SUCCESS".equals((String) packageParams.get("result_code"))) {
                // 这里是支付成功  
                //////////执行自己的业务逻辑////////////////
                String app_id = (String)packageParams.get("appid");
                String mch_id = (String) packageParams.get("mch_id");
                String openid = (String) packageParams.get("openid");
                String is_subscribe = (String) packageParams.get("is_subscribe");//是否关注公众号
                String out_trade_no = (String) packageParams.get("out_trade_no");//商户订单号

                //付款金额【以分为单位】
                String total_fee = (String) packageParams.get("total_fee");
                //附加参数【商标申请_0bda32824db44d6f9611f1047829fa3b_15460】--【业务类型_会员ID_订单号】
                String attach = (String)packageParams.get("attach");

                //微信生成的交易订单号
                String transaction_id = (String)packageParams.get("transaction_id");//微信支付订单号
                //////////执行自己的业务逻辑//////////////// 
                //暂时使用最简单的业务逻辑来处理：只是将业务处理结果保存到session中
                //（根据自己的实际业务逻辑来调整，很多时候，我们会操作业务表，将返回成功的状态保留下来）
                request.getSession().setAttribute("_PAY_RESULT", "OK");
                request.setAttribute("PAY_RESULT", "OK");

                System.out.println("支付成功");
                //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.  
                resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                        + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                numJi = 2;

            } else {
                //支付不成功
                resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                        + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            }
            //------------------------------  
            //处理业务完毕  
            //------------------------------  
            BufferedOutputStream out = new BufferedOutputStream(
                    response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();
        } else {
            System.out.println("通知签名验证失败");
        }
    }

    /**
     * 自己再写个判断的
     * @param user
     * @param request
     * @param response
     * @param modelMap
     * @return
     */
    @ResponseBody
    @RequestMapping("/hadPayed")
    public Map<String, Object> hadPayed(UserVO user, HttpServletRequest request, HttpServletResponse response,
                                      ModelMap modelMap) {
        //读取参数
        InputStream inputStream;
        StringBuffer sb = new StringBuffer();
        String s;
        BufferedReader in;
        //过滤空 设置 TreeMap
        SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();

        try {
            inputStream = request.getInputStream();
            in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
            inputStream.close();

            //解析xml成map
            Map<String, String> m = new HashMap<String, String>();
            m = XMLUtil4jdom.doXMLParse(sb.toString());


            if(m == null){
//                return error("没成功");
                return null;
            }
            Iterator it = m.keySet().iterator();
            while (it.hasNext()) {
                String parameter = (String) it.next();
                String parameterValue = m.get(parameter);

                String v = "";
                if (null != parameterValue) {
                    v = parameterValue.trim();
                }
                packageParams.put(parameter, v);
            }
        } catch (IOException e) {
        } catch (JDOMException e) {
        }

        // 微信支付的API密钥
        String key = PayConfigUtil.API_KEY; //key
        //微信支付返回来的参数
        System.out.println(packageParams);
        //判断签名是否正确
        if (PayToolUtil.isTenpaySign("UTF-8", packageParams, key)) {
            //------------------------------
            //处理业务开始
            //------------------------------
            String resXml = "";
            if ("SUCCESS".equals((String) packageParams.get("result_code"))) {
                return success("支付成功！");
            }
        }
        return null;
    }

    @ResponseBody
    @RequestMapping("/getnum")
    public int getNumJi(){
        return numJi;
    }


}
