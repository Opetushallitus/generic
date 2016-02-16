package fi.vm.sade.generic.rest;

import java.io.*;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

public class UrlProperties {
    private static Properties systemUrlProperties = resolveSystemProperties();
    private final Properties urlProperties;
    public final Properties defaults = new Properties();
    public final Properties defaultOverrides = new Properties();

    public UrlProperties(String file) {
        urlProperties = loadPropertiesFromResource(file);
    }

    private Properties loadPropertiesFromResource(String file) {
        return loadProperties(this.getClass().getResourceAsStream(file));
    }

    private static Properties loadPropertiesFromPath(String path) {
        try {
            return loadProperties(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties loadProperties(InputStream inputStream) {
        try {
            final Properties properties = new Properties();
            try {
                properties.load(inputStream);
                return properties;
            } finally {
                if(inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties resolveSystemProperties() {
        Properties dest = new Properties();
        Properties system = System.getProperties();
        if(system.containsKey("url-properties")) {
            for(String path: system.getProperty("url-properties").split(",")) {
                Properties file = loadPropertiesFromPath(path);
                merge(dest, file);
            }
        }
        merge(dest, getSystemUrlProperties());
        return dest;
    }

    private static Properties getSystemUrlProperties() {
        Properties system = System.getProperties();
        Properties dest = new Properties();
        for(String key: system.stringPropertyNames()) {
            if(key.startsWith("url.")) {
                String destKey = key.substring(4);
                dest.setProperty(destKey, system.getProperty(key));
            }
        }
        return dest;
    }

    public UrlProperties(Properties urlProperties) {
        this.urlProperties = urlProperties;
    }

    public String url(String key, Object... params) {
        return new UrlResolver().url(key, params);
    }

    public UrlResolver urls(Object... args) {
        Properties urlsConfig = new Properties();
        for(Object o: args) {
            if(o instanceof Map) {
                merge(urlsConfig, (Map) o);
            } else if(o instanceof String) {
                urlsConfig.put("baseUrl", o);
            }
        }
        return new UrlResolver(urlsConfig);
    }

    private static void merge(Map dest, Map map) {
        for(Object key: map.keySet()) {
            dest.put(key, map.get(key));
        }
    }

    public class UrlResolver {
        private final Properties urlsConfig = new Properties();
        private boolean encode = true;

        public UrlResolver(Properties urlsConfig) {
            merge(this.urlsConfig, urlsConfig);
        }

        public UrlResolver() {
        }

        private Object resolveConfig(String key) {
            return resolveConfig(key, null);
        }

        private Object resolveConfig(String key, String defaultValue) {
            for (Properties props : new Properties[]{urlsConfig, defaultOverrides, urlProperties, defaults, systemUrlProperties, System.getProperties()}) {
                if (props.containsKey(key)) {
                    return props.get(key);
                }
            }
            return defaultValue;
        }

        public UrlResolver baseUrl(String baseUrl) {
            urlsConfig.put("baseUrl", baseUrl);
            return this;
        }

        public UrlResolver noEncoding() {
            encode = false;
            return this;
        }

        public String url(String key, Object... params) {
            Object o = resolveConfig(key);
            if (o == null) {
                throw new RuntimeException("Could not resolve value for '" + key + "'");
            }
            String url = replaceParams(o.toString(), params);
            Object baseUrl = resolveConfig(parseService(key) + ".baseUrl");
            if (baseUrl == null) {
                baseUrl = resolveConfig("baseUrl");
            }
            if (baseUrl != null) {
                url = joinUrl(baseUrl.toString(), url);
            }
            return url;
        }

        private String replaceParams(String url, Object... params) {
            String queryString = "";
            for (int i = params.length; i > 0; i--) {
                Object param = params[i - 1];
                if (param instanceof Map) {
                    Map paramMap = (Map) param;
                    for (Object key : paramMap.keySet()) {
                        Object o = paramMap.get(key);
                        String value = enc(o);
                        String keyString = enc(key);
                        String tmpUrl = url.replace("$" + keyString, value);
                        if (tmpUrl.equals(url)) {
                            if (queryString.length() > 0) {
                                queryString = queryString + "&";
                            } else {
                                queryString = "?";
                            }
                            queryString = queryString + keyString + "=" + value;
                        }
                        url = tmpUrl;
                    }
                } else {
                    url = url.replace("$" + i, enc(param));
                }
            }
            return url + queryString;
        }

        private String enc(Object key) {
            String s = key == null ? "" : key.toString();
            if (encode) {
                try {
                    s = URLEncoder.encode(s, "UTF-8")
                            .replaceAll("\\+", "%20")
                            .replaceAll("\\%21", "!")
                            .replaceAll("\\%27", "'")
                            .replaceAll("\\%28", "(")
                            .replaceAll("\\%29", ")")
                            .replaceAll("\\%7E", "~");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            return s;
        }

        private String joinUrl(String... urls) {
            if (urls.length == 0) {
                throw new RuntimeException("no arguments");
            }
            String url = null;
            for (String arg : urls) {
                if (url == null) {
                    url = arg;
                } else {
                    if (url.endsWith("/") || arg.startsWith("/")) {
                        url = url + arg;
                    } else {
                        url = url + "/" + arg;
                    }
                }
            }
            return url;
        }

        private String parseService(String key) {
            return key.substring(0, key.indexOf("."));
        }
    }
}
