package com.cg.hbase.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置文件
 * @author Rex.Tan
 * @date 2019年7月25日 下午3:42:44
 */
public class Property {
	private static final Logger LOG = LoggerFactory.getLogger(Property.class);
	private final static String CONF_NAME = "config.properties";
	private static Properties contextProperties;
	static {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONF_NAME);
		contextProperties = new Properties();
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(in, "UTF-8");
			contextProperties.load(inputStreamReader);
		} catch (IOException e) {
			LOG.error("=============资源文件加载失败!", e);
			e.printStackTrace();
		}
		LOG.info("=============资源文件加载成功!");
	}

	public static String getStrValue(String key) {
		return contextProperties.getProperty(key);
	}

	public static int getIntValue(String key) {
		String strValue = getStrValue(key);
		return Integer.parseInt(strValue);
	}

	public static Properties getKafkaProperties(String groupId) {
		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", getStrValue("kafka.bootstrap.servers"));
		properties.setProperty("zookeeper.connect", getStrValue("kafka.zookeeper.connect"));
		properties.setProperty("group.id", groupId);
		return properties;
	}

}