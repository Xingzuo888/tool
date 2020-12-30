/** 
 * Description: For varify MD5 values of Download File
 * @Copyright: Copyright (c) 2012
 * @Company: Amlogic
 * @version: 1.0
 */
package com.wxz.libnetdisc.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    static String TAG = "MD5";
    public final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",  
        "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };  
    public static String createMd5(File file) {
        MessageDigest mMDigest;
        FileInputStream Input;
        byte buffer[] = new byte[1024];
        int len;
        if (!file.exists()){
            return null;
        }
        try {
            mMDigest = MessageDigest.getInstance("MD5");
            Input = new FileInputStream(file);
            while ((len = Input.read(buffer, 0, 1024)) != -1) {
                mMDigest.update(buffer, 0, len);
            }
            Input.close();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        String MD5 = byteArrayToHexString(mMDigest.digest());
        Log.v("OTA", "create_MD5=" + MD5);
        return MD5;

    }
    public static String createMd5(byte[] bytes) {
        MessageDigest mMDigest;
        if (bytes.length<=0){
            return null;
        }
        try {
            mMDigest = MessageDigest.getInstance("MD5");
                mMDigest.reset();
                mMDigest.update(bytes);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        String MD5 = byteArrayToHexString(mMDigest.digest());
        Log.v("OTA", "create_MD5=" + MD5);
        return MD5;

    }
    public static boolean checkMd5(String Md5,File file){
    	Log.d("OTA",file.getPath());
    String str = createMd5(file);
        if(Md5.equalsIgnoreCase(str)){
            Log.d(TAG,"md5sum = " + str+"Md5="+Md5);
            return true;
		}else if(Md5.equalsIgnoreCase("0" + str)){//fix bug
			Log.d(TAG,"md5sum = " + ("0" + str) +"Md5="+Md5);
			return true;
        }else{
            Log.d(TAG," not equals md5sum = " + str+"Md5="+Md5);
            return false;
        }
    }
    public static boolean checkMd5(String Md5, String strfile) {

        File file = new File(strfile);
        return checkMd5(Md5,file);
    }

    /** 
     * 字节数组转十六进制字符串 
     * @param digest 
     * @return 
     */  
    private static String byteArrayToHexString(byte[] digest) {  
        StringBuffer buffer = new StringBuffer();  
        for(int i=0; i<digest.length; i++){  
            buffer.append(byteToHexString(digest[i]));  
        }  
        Log.d("OTA", "buffer.toString()" + buffer.toString()) ;
        return buffer.toString();  
    }  
    /** 
     * 字节转十六进制字符串 
     * @param b 
     * @return 
     */  
    private static String byteToHexString(byte b) {  
        //  int d1 = n/16;  
             int d1 = (b&0xf0)>>4;  
        //   int d2 = n%16;  
             int d2 = b&0xf;  
             return hexDigits[d1] + hexDigits[d2];  
    }  
}
