package com.gic.fadv.online.utility;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public class Utility {
	public static void printGettersSetters(Class aClass){
		  Method[] methods = aClass.getMethods();

		  for(Method method : methods){
		    if(isGetter(method)) System.out.println("getter: " + method);
		    if(isSetter(method)) System.out.println("setter: " + method);
		  }
		}

		public static boolean isGetter(Method method){
		  if(!method.getName().startsWith("get"))      return false;
		  if(method.getParameterTypes().length != 0)   return false;  
		  if(void.class.equals(method.getReturnType())) return false;
		  return true;
		}

		public static boolean isSetter(Method method){
		  if(!method.getName().startsWith("set")) return false;
		  if(method.getParameterTypes().length != 1) return false;
		  return true;
		}
		
		public static boolean checkContains(String str,String str1){
			//shortArticle.toLowerCase().contains(personalInfoSearch.getName().toLowerCase())
			if(str == null || str1 == null) {
				return false;
			}
			return str.toLowerCase().contains(str1.toLowerCase());
		}
		
		private void timeOut() {
			try { 
				TimeUnit.SECONDS.sleep(100); 
			} catch (InterruptedException ie) 
			{
			  Thread.currentThread().interrupt(); 
			}
		}
		
		public static String formatDateUtil(String dateStr) {
			SimpleDateFormat formatter1 = new SimpleDateFormat("MM/dd/yyyy");
			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");

			try {
				if (dateStr != null && StringUtils.isNotBlank(dateStr)) {
					Date checkDate = formatter1.parse(dateStr);
					return formatter2.format(checkDate);
				}
			} catch (ParseException e) {
				return dateStr;
			}
			return dateStr;
		}
}
