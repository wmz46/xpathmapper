import com.iceolive.xpathmapper.XPathMapper;
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
    public void test1(){
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
        Student student = XPathMapper.parse(xml,Student.class);
        String newXml = XPathMapper.format(student, true);
        Student student1 = XPathMapper.parse(newXml,Student.class);
        Assert.assertEquals("经过反序列化->序列化->反序列化后两次对象相等",student,student1);
    }
}
