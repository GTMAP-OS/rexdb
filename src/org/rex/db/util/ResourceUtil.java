package org.rex.db.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.rex.db.exception.DBException;
import org.rex.db.logger.Logger;
import org.rex.db.logger.LoggerFactory;

/**
 * 用于加载配置文件
 */
public class ResourceUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);

	private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

	/**
	 * 从类路径中加载输入流
	 */
	public static InputStream getResourceAsStream(String resource) throws DBException {
		return getResourceAsStream(null, resource);
	}

	public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws DBException {
		InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
		if (in == null) {
			throw new DBException("DB-U0001", resource);
		}
		return in;
	}

	/**
	 * 从类路径中加载配置
	 */
	public static Properties getResourceAsProperties(String resource) throws DBException {
		return getResourceAsProperties(null, resource, null);
	}
	
	public static Properties getResourceAsProperties(String resource, String encoding) throws DBException {
		return getResourceAsProperties(null, resource, encoding);
	}

	public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws DBException {
		Reader reader = getResourceAsReader(loader, resource);
		return getProperties(resource, reader);
	}
	
	public static Properties getResourceAsProperties(ClassLoader loader, String resource, String encoding) throws DBException {
		Reader reader = getResourceAsReader(loader, resource, encoding);
		return getProperties(resource, reader);
	}

	/**
	 * 从类路径中加载Reader
	 */
	public static Reader getResourceAsReader(String resource) throws DBException {
		return getResourceAsReader(null, resource, null);
	}

	public static Reader getResourceAsReader(String resource, String encoding) throws DBException {
		return getResourceAsReader(null, resource, encoding);
	}
	
	public static Reader getResourceAsReader(ClassLoader loader, String resource) throws DBException {
		return getResourceAsReader(loader, resource, null);
	}
	
	public static Reader getResourceAsReader(ClassLoader loader, String resource, String encoding) throws DBException {
		if(encoding != null){
			try {
				return new InputStreamReader(getResourceAsStream(loader, resource), encoding);
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn("Error on loading resource {0} as {1}, {2}, the resource will be loaded as default encoding.", resource, encoding, e.getMessage());
			}
		}
		
		return new InputStreamReader(getResourceAsStream(loader, resource));
	}

	/**
	 * 从类路径中加载File
	 */
	public static File getResourceAsFile(String resource) throws DBException {
		return getResourceAsFile(null, resource);
	}

	public static File getResourceAsFile(ClassLoader loader, String resource) throws DBException {
		return new File(getResourceURL(loader, resource).getFile());
	}

	/**
	 * 从URL中加载资源
	 */
	public static URL getResourceURL(String resource) throws DBException {
		return getResourceURL(null, resource);
	}

	public static URL getResourceURL(ClassLoader loader, String resource) throws DBException {
		URL url = classLoaderWrapper.getResourceAsURL(resource, loader);
		if (url == null) {
			throw new DBException("DB-U0001", resource);
		}
		return url;
	}

	/**
	 * 从URL中加载输入流
	 */
	public static InputStream getUrlAsStream(String urlString) throws DBException {
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			return conn.getInputStream();
		} catch (IOException e) {
			throw new DBException("DB-U0002", e, urlString, e.getMessage());
		}
	}

	/**
	 * 从URL中加载Reader
	 */
	public static Reader getUrlAsReader(String urlString) throws DBException {
		return getUrlAsReader(urlString, null);
	}
	
	public static Reader getUrlAsReader(String urlString, String encoding) throws DBException {
		if(encoding != null){
			try {
				return new InputStreamReader(getUrlAsStream(urlString), encoding);
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn("Error on loading url{0} as {1}, {2}, the url will be loaded as default encoding.", urlString, encoding, e.getMessage());
			}
		}
		return new InputStreamReader(getUrlAsStream(urlString));
	}

	public static Properties getUrlAsProperties(String urlString) throws DBException {
		return getUrlAsProperties(urlString, null);
	}
	
	public static Properties getUrlAsProperties(String urlString, String encoding) throws DBException {
		Reader reader = getUrlAsReader(urlString, encoding);
		return getProperties(urlString, reader);
	}
	

	private static Properties getProperties(String path, Reader reader) throws DBException {
		Properties props = new Properties();
		try {
			props.load(reader);
		} catch (IOException e) {
			throw new DBException("DB-U0002", e, path, e.getMessage());
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LOGGER.warn("Error on closing input stream of {0}, {1}.", path, e.getMessage());
			}
		}

		return props;
	}
	


	/**
	 * 使用类加载器加载文件
	 */
	static class ClassLoaderWrapper {

		ClassLoader systemClassLoader;

		ClassLoaderWrapper() {
			try {
				systemClassLoader = ClassLoader.getSystemClassLoader();
			} catch (SecurityException ignored) {
			}
		}

		public URL getResourceAsURL(String resource) {
			return getResourceAsURL(resource, getClassLoaders(null));
		}

		public URL getResourceAsURL(String resource, ClassLoader classLoader) {
			return getResourceAsURL(resource, getClassLoaders(classLoader));
		}

		public InputStream getResourceAsStream(String resource) {
			return getResourceAsStream(resource, getClassLoaders(null));
		}

		public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
			return getResourceAsStream(resource, getClassLoaders(classLoader));
		}

		InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
			for (ClassLoader cl : classLoader) {
				if (null != cl) {
					InputStream returnValue = cl.getResourceAsStream(resource);
					if (null == returnValue) {
						returnValue = cl.getResourceAsStream("/" + resource);
					}
					if (null != returnValue) {
						return returnValue;
					}
				}
			}
			return null;
		}

		URL getResourceAsURL(String resource, ClassLoader[] classLoader) {
			URL url;
			for (ClassLoader cl : classLoader) {
				if (cl != null) {
					url = cl.getResource(resource);
					if (url == null) {
						url = cl.getResource("/" + resource);
					}
					if (url != null) {
						return url;
					}
				}
			}
			return null;
		}

		ClassLoader[] getClassLoaders(ClassLoader classLoader) {
			return new ClassLoader[] { classLoader, Thread.currentThread().getContextClassLoader(), getClass().getClassLoader(), systemClassLoader };
		}

	}
}
