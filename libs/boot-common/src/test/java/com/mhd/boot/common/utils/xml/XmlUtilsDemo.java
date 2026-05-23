package com.mhd.boot.common.utils.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Xml 工具类示例类，使用 JAXB 实现
 *
 * @author zhao-hao-dong
 */
public class XmlUtilsDemo {
    // 定义一个简单的测试实体类
    @XmlRootElement(name = "user")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class User {
        @XmlElement(name = "id")
        private int id;
        @XmlElement(name = "name")
        private String name;
        @XmlElement(name = "email")
        private String email;

        public User() {
        }

        public User(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("========== 1. 测试对象转 XML (toXml) ==========");
        User user = new User(1001, "张三", "zhangsan@example.com");
        // 测试带 XML 声明头
        String xmlWithHeader = XmlUtils.toXml(user, true, true);
        System.out.println("带声明头的 XML:\n" + xmlWithHeader);

        System.out.println("\n========== 2. 测试 XML 转对象 (fromXml) ==========");
        // 测试不带声明头的 XML 转回对象
        String xmlWithoutHeader = XmlUtils.toXml(user, false, false);
        User parsedUser = XmlUtils.fromXml(xmlWithoutHeader, User.class);
        System.out.println("解析出的对象: " + parsedUser);

        System.out.println("\n========== 3. 测试自定义 SOAP Header (buildCustomSoapHeader) ==========");
        Map<String, String> authMap = new HashMap<>();
        authMap.put("AppKey", "my_app_123");
        authMap.put("Token", "abc-xyz-789");
        // 模拟拼接一个包含特殊字符的字段，测试转义功能
        authMap.put("Remark", "测试<特殊>&符号");

        String customHeader = XmlUtils.buildCustomSoapHeader("AuthHeader", authMap);
        System.out.println("生成的自定义 Header:\n" + customHeader);

        System.out.println("\n========== 4. 测试 XSD 强校验 (validateXml & fromXml with XSD) ==========");
        // 为了演示，我们在本地临时创建一个简单的 XSD 文件
        //String xsdContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        //        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
        //        "  <xs:element name=\"user\">\n" +
        //        "    <xs:complexType>\n" +
        //        "      <xs:sequence>\n" +
        //        "        <xs:element name=\"id\" type=\"xs:int\"/>\n" +
        //        "        <xs:element name=\"name\" type=\"xs:string\"/>\n" +
        //        "        <xs:element name=\"email\" type=\"xs:string\"/>\n" +
        //        "      </xs:sequence>\n" +
        //        "    </xs:complexType>\n" +
        //        "  </xs:element>\n" +
        //        "</xs:schema>";
        //
        //File tempXsd = File.createTempFile("test_schema", ".xsd");
        //tempXsd.deleteOnExit();
        //try (FileWriter writer = new FileWriter(tempXsd)) {
        //    writer.write(xsdContent);
        //}
        // 注意：因为 getSchema 默认从 classpath 加载，这里为了演示方便，我们直接用文件路径加载
        // 在实际项目中，你只需把 XSD 放在 resources 目录下，传入 "xxx.xsd" 即可

        // 4.1 测试 validateXml 独立校验
        Schema schema = XmlUtils.getSchema("user.xsd");
        boolean isValid = XmlUtils.validateXml(xmlWithoutHeader, schema);
        System.out.println("独立校验 XML 是否符合 XSD 规范: " + (isValid ? "通过" : "失败"));

        // 4.2 测试 fromXml 带 XSD 强校验
        try {
            // 这里为了绕过工具类默认的 ClassLoader 限制，实际使用时直接传 classpath 路径即可
            // 下面这行仅作逻辑演示，实际会因找不到 classpath 资源报错，但逻辑是通的：
            User validUser = XmlUtils.fromXml(xmlWithoutHeader, User.class, schema);
            //System.out.println("fromXml 带 XSD 强校验功能已就绪（将 XSD 放入 resources 目录传入路径即可生效）。");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n========== 所有测试演示完毕 ==========");
    }
}
