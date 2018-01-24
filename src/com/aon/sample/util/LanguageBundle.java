package com.aon.sample.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import com.aon.sample.ICCReconciliationConstants;

public class LanguageBundle {

	public static HashMap<Locale,ResourceBundle> labelNameBundleMap =new HashMap<Locale, ResourceBundle>();

	public static String getLabelNameValues(String key,Locale locale){
		try{
			String label = "";
			if(labelNameBundleMap.containsKey(locale)){
				ResourceBundle bundle = labelNameBundleMap.get(locale);
				label = bundle.getString(key);
				return label;
			}
			else {
				ResourceBundle bundle  = ResourceBundle.getBundle(ICCReconciliationConstants.MESSAGE_PROPERTY_FILE, locale);
				labelNameBundleMap.put(locale, bundle);
				label = bundle.getString(key);
				return label;
			}
		}
		catch(Exception e){
			//Do Nothing
			
			System.out.println("error r ");
			e.printStackTrace();
		}
		return key;
	}



}
