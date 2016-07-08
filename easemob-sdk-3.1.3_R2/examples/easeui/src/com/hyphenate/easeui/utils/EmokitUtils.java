package com.hyphenate.easeui.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 情绪识别
 * 
 * @author tom
 */
public class EmokitUtils {
	
    private static final int TIME_OUT = 10 * 1000; // 超时时间
    private static final String CHARSET = "utf-8"; // 设置编码
	 
    private volatile static EmokitUtils mEmokitUtils;
    
    private int appid;
    
    private String  key, uid;
    
    public static EmokitUtils getInstance(Context context) {
    	
    		if(mEmokitUtils == null) {
    			synchronized (EmokitUtils.class) {

    				if (mEmokitUtils == null) {
    					mEmokitUtils = new EmokitUtils(context);
    				}

    			}
    		}
    		
    		return mEmokitUtils;
    }
	  
    private EmokitUtils(Context context) {
    		
    		ApplicationInfo appInfo;
		
		try {
			appInfo = context.getPackageManager()
			            .getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA);
			if(appInfo.metaData.containsKey("EMOKIT_AID"))
				appid=	appInfo.metaData.getInt("EMOKIT_AID");
			if(appInfo.metaData.containsKey("EMOKIT_KEY"))
				key =appInfo.metaData.getString("EMOKIT_KEY");

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		// 初始化设备信息
		TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			if (telMgr.getDeviceId() != null
					&& !telMgr.getDeviceId().equals("")) {
				uid = telMgr.getDeviceId();
			} else {
				uid = "100000000000000";
			}

		} catch (Exception e) {
			uid = "100000000000000";
		}
    }
    
    /**
     * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
     * 
     * @param url Service net address
     * @param params text content
     * @param files pictures
     * @return String result of Service response
     * @throws IOException
     */
    public  String getEmo(String filePath) 
            throws Exception {
    	
    		String filesplit[] = filePath.split("/");
    		String fileName = filesplit[filesplit.length-1];
    		
    		Log.e("getEmo", fileName + "-------" +filePath);
    		
    		String url = "http://api-web.emokit.com:802/wechatemo/WxVoiceamr.do";
    		Map<String, String> params = new HashMap<String, String>();
		params.put("appid", appid+"");
		params.put("key", key);
		params.put("platid", "android huanxin");
		params.put("uid", uid);
		
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        URL uri = new URL(url);
        HttpURLConnection	conn = (HttpURLConnection) uri.openConnection();
		
		conn.setConnectTimeout(TIME_OUT);
		//conn.setRequestMethod("POST");
        //HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(TIME_OUT); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
       
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", CHARSET);
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
        // 首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
        }
        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());

        outStream.write(sb.toString().getBytes());
        // 发送文件数据

            
        StringBuilder sb1 = new StringBuilder();
        sb1.append(PREFIX);
        sb1.append(BOUNDARY);
        sb1.append(LINEND);
        sb1.append("Content-Disposition: form-data; name=\"upload\"; filename=\""+fileName+"\"" + LINEND);
        sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
        sb1.append(LINEND);
        outStream.write(sb1.toString().getBytes());


        InputStream is = new FileInputStream(filePath);
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        is.close();
        outStream.write(LINEND.getBytes());
            


        // 请求结束标志
        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
        outStream.write(end_data);
        outStream.flush();
        // 得到响应码
        int res = conn.getResponseCode();
        InputStream in = conn.getInputStream();
        
        String result  = "";
        if (res == 200) {
        		ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = in.read()) != -1) {
				baos.write(i);
			}


			String str = new String(baos.toByteArray(), CHARSET);
			str = str.replace('\n', ' ');
			str = str.replace('\r', ' ');
			
			result = str;
			
			Log.e("result", str);
				
        }
        outStream.close();
        conn.disconnect();
        
        try {
        JSONObject resultJsonObject = new JSONObject(result);
		JSONObject jsonobj = resultJsonObject.getJSONObject("infovoice");
		
		int retcode = jsonobj.getInt("resultcode");
		if(retcode == 200) {
			
			// 存储情绪数据
			PropertiesConfig.getInstance(PropertiesConfig.SD_CARD).setProperty(fileName, jsonobj.getString("rc_main")); 
			
			
		} else {
			// 存储情绪数据
			PropertiesConfig.getInstance(PropertiesConfig.SD_CARD).setProperty(fileName, "--"); 
			
		}
		
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	
        return result;
    }
    

}