package com.mhd.boot.common.utils.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Xml 工具类，使用 JAXB 实现
 *
 * @author zhao-hao-dong
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class XmlUtils {
    /**
     * 缓存 JAXBContext，避免每次读取并解析类上的 JAXB 注解。Key = 类对象，Value = JAXBContext 实例
     */
    private static final ConcurrentHashMap<Class<?>, JAXBContext> JAXB_CONTEXT_CACHE = new ConcurrentHashMap<>();
    /**
     * 缓存 Schema，避免每次读取 XSD。Key = XSD 文件路径，Value = Schema 实例
     */
    private static final ConcurrentHashMap<String, Schema> SCHEMA_CACHE = new ConcurrentHashMap<>();

    /**
     * Java 对象转 XML 字符串（默认 UTF-8，无 XML 声明头，带格式化）
     */
    public static String toXml(Object obj) {
        return toXml(obj, true, false);
    }

    /**
     * Java 对象转 XML 字符串（全参数定制）
     *
     * @param obj                   待转换对象
     * @param formatted             是否格式化输出（带缩进和换行）
     * @param includeXmlDeclaration 是否包含 <?xml version="1.0" encoding="UTF-8"?> 声明
     */
    public static String toXml(Object obj, boolean formatted, boolean includeXmlDeclaration) {
        try {
            JAXBContext context = getJAXBContext(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.toString());
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            // JAXB_FRAGMENT 为 true 时，会去掉 XML 声明头。
            // 这里设置始终不输出 XML 声明头，由调用方根据需要自行添加，因为默认生成的 声明头 中包含 standalone="yes"。
            //marshaller.setProperty(Marshaller.JAXB_FRAGMENT, !includeXmlDeclaration);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            String result = writer.toString();

            // 如果调用方明确要求包含 XML 声明，我们手动在最前面拼接一个最纯净的声明头
            if (includeXmlDeclaration) {
                result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() + result;
            }
            return result;
        } catch (JAXBException e) {
            throw new RuntimeException("对象转 XML 失败: " + e.getMessage(), e);
        }
    }

    /**
     * XML 字符串转 Java 对象
     */
    public static <T> T fromXml(String xml, Class<T> clazz) {
        return fromXml(xml, clazz, null);
    }

    /**
     * XML 字符串转 Java 对象（支持传入编译好的 Schema 对象进行强校验）
     *
     * @param xml    XML 字符串
     * @param clazz  目标 Class
     * @param schema 编译好的 XSD Schema 对象（传 null 则跳过校验）
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXml(String xml, Class<T> clazz, Schema schema) {
        try {
            JAXBContext context = getJAXBContext(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // 如果传入了 Schema 对象，直接进行强校验
            if (schema != null) {
                unmarshaller.setSchema(schema);
            }

            return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new RuntimeException("XML 转对象失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建自定义 SOAP 协议头 （适用于身份认证、事务追踪等场景）
     * 示例：传入 Map.of("username", "admin", "password", "123456") 和 rootElementName="Security"
     * 生成：<Security><username>admin</username><password>123456</password></Security>
     *
     * @param rootElementName 根元素名称，例如 "Security"
     * @param elements        要包含在 SOAP 头中的元素，Key = 元素名称，Value = 元素值
     * @return 构建好的 SOAP 头字符串
     */
    public static String buildCustomSoapHeader(String rootElementName, Map<String, String> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(rootElementName).append(">");
        if (elements != null && !elements.isEmpty()) {
            for (Map.Entry<String, String> entry : elements.entrySet()) {
                sb.append("<").append(entry.getKey()).append(">")
                        .append(escapeXml(entry.getValue())) // 防止 XML 注入
                        .append("</").append(entry.getKey()).append(">");
            }
        }
        sb.append("</").append(rootElementName).append(">");
        return sb.toString();
    }

    /**
     * XML 格式校验工具（校验是否符合 XSD 规范）
     *
     * @param xml    XML 字符串
     * @param schema 编译好的 XSD Schema 对象
     * @return true = XML 符合 XSD 规范，false = 不符合或校验过程中发生异常
     */
    public static boolean validateXml(String xml, Schema schema) {
        try {
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml)));
            return true;
        } catch (Exception e) {
            log.error("XML 校验失败: {}", e.getMessage());
            return false;
        }
    }


    /**
     * 获取 Schema 对象，通过 classpath 相对路径加载（例如 "schema/user.xsd"）
     *
     * @param xsdPath XSD 文件在 classpath 中的相对路径，例如 "schema/user.xsd"
     * @return Schema 对象
     */
    public static Schema getSchema(String xsdPath) {
        return SCHEMA_CACHE.computeIfAbsent("path:" + xsdPath, path -> {
            try {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                java.net.URL url = XmlUtils.class.getClassLoader().getResource(xsdPath);
                if (url == null) {
                    throw new RuntimeException("在 classpath 中未找到 XSD 文件: " + xsdPath);
                }
                return factory.newSchema(url);
            } catch (Exception e) {
                throw new RuntimeException("加载或解析 XSD 失败: " + xsdPath, e);
            }
        });
    }

    /**
     * 获取 Schema 对象，通过绝对路径或相对文件路径加载（例如 "D:/config/user.xsd"）
     *
     * @param filePath XSD 文件的绝对路径或相对文件
     * @return Schema 对象
     */
    public static Schema getSchemaByFile(String filePath) {
        return SCHEMA_CACHE.computeIfAbsent("file:" + filePath, path -> {
            try {
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) {
                    throw new RuntimeException("未找到 XSD 文件: " + filePath);
                }
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                return factory.newSchema(file);
            } catch (Exception e) {
                throw new RuntimeException("加载或解析 XSD 文件失败: " + filePath, e);
            }
        });
    }

    /**
     * 获取 Schema 对象，通过字节流（InputStream）加载
     *
     * @param inputStream XSD 字节流（InputStream）
     * @param schemaId    用于缓存的唯一标识符，例如 "userSchema"，确保同一 schemaId 对应同一 Schema 对象
     * @return Schema 对象
     */
    public static Schema getSchemaByStream(InputStream inputStream, String schemaId) {
        return SCHEMA_CACHE.computeIfAbsent("stream:" + schemaId, id -> {
            try {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                return factory.newSchema(new StreamSource(inputStream));
            } catch (Exception e) {
                throw new RuntimeException("通过流加载或解析 XSD 失败: " + schemaId, e);
            }
        });
    }

    /**
     * 获取 Schema 对象，通过字节数组（byte[]）加载
     *
     * @param xsdBytes XSD 字节数组（byte[]）
     * @param schemaId 用于缓存的唯一标识符，例如 "userSchema"，确保同一 schemaId 对应同一 Schema 对象
     * @return Schema 对象
     */
    public static Schema getSchemaByBytes(byte[] xsdBytes, String schemaId) {
        return SCHEMA_CACHE.computeIfAbsent("bytes:" + schemaId, id -> {
            try {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                return factory.newSchema(new StreamSource(new ByteArrayInputStream(xsdBytes)));
            } catch (Exception e) {
                throw new RuntimeException("通过字节数组加载或解析 XSD 失败: " + schemaId, e);
            }
        });
    }

    // ================== 私有缓存与基础方法 ==================

    private static JAXBContext getJAXBContext(Class<?> clazz) throws JAXBException {
        return JAXB_CONTEXT_CACHE.computeIfAbsent(clazz, c -> {
            try {
                return JAXBContext.newInstance(c);
            } catch (JAXBException e) {
                // 解决 Lambda 表达式中无法抛出受检异常的问题
                throw new RuntimeException("创建 JAXBContext 失败: " + c.getName(), e);
            }
        });
    }

    // 简单的 XML 特殊字符转义，防止自定义 Header 时出现格式错误
    private static String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
