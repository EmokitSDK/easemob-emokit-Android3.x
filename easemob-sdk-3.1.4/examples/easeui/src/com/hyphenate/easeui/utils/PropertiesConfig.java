package com.hyphenate.easeui.utils;

import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileNotFoundException; 
import java.io.FileOutputStream; 
import java.io.IOException; 
import java.io.InputStream; 
import java.util.Properties; 
 
public class PropertiesConfig extends Properties { 
 
	public static final String SD_CARD = "/sdcard/client_config.xml";
	
    String propertyPath=""; 
    private PropertiesConfig(String path) { 
        propertyPath=path; 
    }; 
 
    public static PropertiesConfig getInstance(String path) { 
        { 
            File file = new File(path); 
            if (!file.exists()) { 
                try { 
                    file.createNewFile(); 
                } catch (IOException e) { 
                    e.printStackTrace(); 
                } 
            } 
            PropertiesConfig pro = new PropertiesConfig(path); 
            try { 
                InputStream is = new FileInputStream(file); 
                pro.load(is); 
                is.close(); 
            } catch (Exception e) { 
                e.printStackTrace(); 
            } 
            return pro; 
        } 
         
    } 
 
    @Override 
    public Object setProperty(String key, String value) { 
    	
//    	K	平静；放松；专注；出神；
//    	D	忧愁；疑惑；迷茫；无助；
//    	C	伤感；郁闷；痛心；压抑；
//    	Y	生气；失控；兴奋；宣泄；
//    	M	开心；甜蜜；欢快；舒畅；
//    	W	害怕；焦虑；紧张；激情；
//    	T	厌恶；反感；意外；惊讶；

    	if(value.equals("K")){
    		value = "平静";
    	} else if(value.equals("D")){
    		value = "忧愁";
    	} else if(value.equals("C")){
    		value = "伤感";
    	} else if(value.equals("Y")){
    		value = "生气";
    	} else if(value.equals("M")){
    		value = "开心";
    	} else if(value.equals("W")){
    		value = "害怕";
    	} else if(value.equals("T")){
    		value = "厌恶";
    	} 
    	
        super.setProperty(key, value); 
        try { 
            this.store(new FileOutputStream(this.propertyPath), 
                    "utf-8"); 
        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
        return value; 
    } 
 
    public Object put(String key, String value) { 
        super.put(key, value); 
        try { 
            this.store(new FileOutputStream(this.propertyPath), 
                    "utf-8"); 
        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
        return value; 
    } 
} 
