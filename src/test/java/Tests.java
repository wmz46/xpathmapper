import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.iceolive.xpathmapper.JsonPathMapper;
import com.iceolive.xpathmapper.XPathMapper;
import com.iceolive.xpathmapper.annotation.JsonPath;
import com.iceolive.xpathmapper.annotation.XPath;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class Tests {

    @Data
    public static class Student {
        @XPath(value = "/input/body/student/name/text()")
        private String name;
        @XPath(value = "/input/body/student/description/text()", CDATA = true)
        private String description;
        @XPath("/input/body/student/@age")
        private int age;
        //布尔型如需特殊处理显示字符串，请设置trueString和falseString，默认为true/false
        @XPath(value = "/input/body/student/@boarding", trueString = "是", falseString = "否")
        private boolean boarding;
        @XPath(value = "/input/body/student/birthday/text()", format = "yyyy年MM月dd日")
        private Date birthday;
        @XPath("/input/body/student/city/@name")
        private String city;
        //对象列表或数组，有取值规则需嵌套类，xpath的最后一个节点路径为循环节点
        @XPath("/input/body/courses/course")
        private List<Course> courses;
        //非对象列表或数组，无需嵌套类，xpath的最后一个节点路径为循环节点
        @XPath("/input/body/tags/tag/text()")
        private String[] tags;
    }

    @Data
    public static class Course {
        //嵌套类的xpath必须为相对路径
        @XPath("./@name")
        private String name;
        @XPath("./text()")
        private int score;
    }

    @Test
    public void test1() {
        String xml = "<input>\n" +
                "    <body>\n" +
                "        <student age=\"20\" boarding=\"否\">\n" +
                "            <name>张三</name>\n" +
                "            <birthday>2000年01月01日</birthday>\n" +
                "            <description><![CDATA[<天行健，君子以自强不息。>]]></description>\n" +
                "            <city name=\"汕头\"/>\n" +
                "        </student>\n" +
                "        <courses>\n" +
                "            <course name=\"语文\">88</course>\n" +
                "            <course name=\"数学\">92</course>\n" +
                "            <course name=\"英语\">64</course>\n" +
                "        </courses>\n" +
                "        <tags>\n" +
                "            <tag>学渣</tag>\n" +
                "            <tag>中二</tag>\n" +
                "            <tag></tag>\n" +
                "        </tags>\n" +
                "    </body>\n" +
                "</input>";
        Student student = XPathMapper.parse(xml, Student.class);
        String newXml = XPathMapper.format(student, true);
        Student student1 = XPathMapper.parse(newXml, Student.class);
        Assert.assertEquals("经过反序列化->序列化->反序列化后两个对象不相等", student, student1);
    }

    @Data
    public static class A {
        @JsonPath("$.a")
        private Byte a;
        @JsonPath("$.b")
        private int[] b;
        @JsonPath("$.c")
        private C c;
        @JsonPath("$.d")
        private List<C> d;
        @JsonPath(value = "$.e")
        //日期类型请定义格式，否则会转换异常
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date e;
        @JsonPath("$.f")
        private char f;
        //当json单个对象嵌套层过多时，这个反序列化工具可将其扁平化处理
        @JsonPath("$.c.e")
        private int c_e;
        @JsonPath("$.c.d")
        private int c_d;
    }

    @Data
    public static class C {
        @JsonPath("$.e")
        private int e;
        @JsonPath("$.d")
        private int d;
    }

    @Test
    public void test2() {
        String json = "{a:1,b:[1,2,2],c:{d:3,e:4},d:[{d:2,e:1},{d:21,e:12}],e:'2019-10-10 01:02:03',f:'j'}";
        //反序列化json字符串
        A a = JsonPathMapper.parse(json, A.class);
        //反序列对象
        Object obj = JSON.parse(json);
        A a1 = JsonPathMapper.parse(obj, A.class);
        Assert.assertEquals("反序列对象和反序列json字符串的两个对象不相等",a,a1);
    }
}
