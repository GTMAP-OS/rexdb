package org.rex.db.exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 加载异常配置文件
 */
public class ExceptionResourceFactory {

	//--异常资源配置文件
	private static final String PROPERTIES_ENCODING = "UTF-8";
			
	//--支持的语言，方便用户设置
	public static final String LANG_ZH_CN = "zh_cn";
	public static final String LANG_EN = "en";
	
	//--异常资源配置文件
	private static final Map<String, String> PROPERTIES = new HashMap<String, String>(){
		{
			put(LANG_ZH_CN, "exception.db.zh-cn.properties");
			put(LANG_EN, "exception.db.en.properties");
		}
	};

	//--当前使用的语言
	private String lang;
	
	//所有资源
	private Map<String, ExceptionResource> resources;
	
	//单例
	private static final ExceptionResourceFactory instance;

	
	static {
		instance = new ExceptionResourceFactory();
	}

	public static ExceptionResourceFactory getInstance() {
		return instance;
	}

	protected ExceptionResourceFactory() {
		resources = new HashMap<String, ExceptionResource>();
		lang = LANG_ZH_CN;
		init();
	}
	
	/**
	 * 加载配置
	 */
	private void init(){
		for (Iterator iterator = PROPERTIES.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
			resources.put(entry.getKey(), 
					new ExceptionResource(loadProperties(entry.getValue(), PROPERTIES_ENCODING)));
		}
	}
	
	/**
	 * 读取一个配置文件
	 */
	private Properties loadProperties(String path, String encode) {
		Properties props = new Properties();
		InputStream inputStream = null;
		try {
			inputStream = getClass().getResourceAsStream(path);
			if (inputStream == null) {
				return null;
			}

			props.load(new InputStreamReader(inputStream, encode));
			return props;
		} catch (IOException ex) {
			throw new RuntimeException("Unable to locate file " + path, ex);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	//--------PUBLIC
	/**
	 * 设置异常语言
	 */
	public void setLang(String lang){
		if(!LANG_ZH_CN.equals(lang) && !LANG_EN.equals(lang))
			throw new RuntimeException("Language "+ lang +" not support, " + LANG_ZH_CN + " or " + LANG_EN + " required.");
		
		this.lang = lang;
	}
	
	/**
	 * 获取异常消息
	 */
	public String translate(String code){
		return translate(code, null);
	}
	
	/**
	 * 获取异常消息
	 */
	public String translate(String code, Object... params){
		
		ExceptionResource resource = resources.get(lang);
		String message = resource.getMessage(code);
		
		//对应错误代码
		if(message == null)
			return code;
		else{
			return "(" + code + ") " + MessageFormat.format(message, params);
		}
	}
}
