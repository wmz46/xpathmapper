# xpathmapper
通过xpath注解对象的方式序列化以及反序列化xml

## 一、当前最新版本
```xml
<dependency>
  <groupId>com.iceolive</groupId>
  <artifactId>xpathmapper</artifactId>
  <version>1.0.1</version>
</dependency>
```
## 二、快速开始
### 举个例子，假如我们需要序列化反序列的xml格式如下
```xml
<input>
    <body>
        <student age="20">
            <name>张三</name>
            <birthday>2000年01月01日</birthday>
            <description><![CDATA[<天行健，君子以自强不息。>]]></description>
            <city name="汕头"/>
        </student>
        <courses>
            <course name="语文">88</course>
            <course name="数学">92</course>
            <course name="英语">64</course>
        </courses>
        <tags>
            <tag>学渣</tag>
            <tag>中二</tag>
            <tag></tag>
        </tags>
    </body>
</input>
```
### 1. 定义类型
```java
@Data
public class Student {
    @XPath(value = "/input/body/student/name/text()")
    private String name;
    @XPath(value="/input/body/student/description/text()",CDATA = true)
    private String description;
    @XPath("/input/body/student/@age")
    private int age;
    @XPath(value = "/input/body/student/birthday/text()",format="yyyy年MM月dd日")
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
public class Course{
    //嵌套类的xpath必须为相对路径
    @XPath("./@name")
    private String name;
    @XPath("./text()")
    private int score;
}
```
### 2.序列化
```java
XPathMapper.format(student, true);//第二个参数表示是否去除xml报文头
```
### 3.反序列化
```java
Student student = XPathMapper.parse(xml,Student.class);
```
### 4.网页内容反序列化
//为避免&符号问题，组件会去除html中的script标签和注释
Student student = XPathMapper.parseHtml(html,Student.class);
### 5.字段支持类型
目前字段支持类型

1.字符串类型：String

2.数值类型：int,float,double,double,Integer,Float,Double,BigDecimal,尚未支持数值类型的格式定义。

3.日期类型：Date,LocalDate,LocalDateTime

4.以上3种类型的数组及列表

5.自定义对象

6.自定义对象的数组及列表


## 三、开发背景
在对接第三方xml报文接口时，发现对方提供的报文结构不太符合我们抽象出来的对象结构。通过JAXBContext虽然也可以实现需求，但可能需要写很多没有实际意义的中间嵌套类，而且赋值取值嵌套层次多的话，除了代码写起来多，而且还得判断中间嵌套层是否为null，避免空指针异常。
 
以上面的xml为例（为避免过于复杂化，此处忽略description和tags节点），假如我们用JAXBContext，需要定义如下类型
```java
@Data
@XmlRootElement(name="input")
@XmlAccessorType(XmlAccessType.FIELD)
public class Input{
    @XmlElement(name="body")
    private Body body;
}
@Data
@XmlRootElement(name="body")
@XmlAccessorType(XmlAccessType.FIELD)
public class Body{
    @XmlElement(name="student")
    private Student student;
    @XmlElementWrapper(name = "courses")
    @XmlElement(name = "course")
    private List<Course> courses;
}
@Data
@XmlRootElement(name="student")
@XmlAccessorType(XmlAccessType.FIELD)
public class Student{
    @XmlElement(name="name")
    private String name;
    @XmlAttribute(name="age")
    private int age;
    @XmlElement(name="city")
    private City city;

}
@Data
@XmlRootElement(name="city")
@XmlAccessorType(XmlAccessType.FIELD)
public class City{
    @XmlAttribute(name="name")
    private String name;
}
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="course")
public static class Course{
    @XmlAttribute(name="name")
    private String name;
    @XmlValue
    private int score;
}
```
明显和上面的处理方式相比，繁琐了很多。

## 四、存在的问题
### 命名空间
首先xpath由于不支持带有命名空间的查询，而且个人觉得xml的命名空间设计过于反人类。所以在反序列时，组件会自动去除xml字符串的命名空间。
### xpath高阶用法
目前只有反序列化的xpath支持带有谓语的路径表达式，比如/a[last()]/b[2]/c。而序列化不支持，后续可以考虑加上带有数组序号的路径表达式的序列化。
### 循环引用的序列化
目前组件没有循环引用检查，如果对象存在循环引用则会导致死循环。如果字段存在循环引用，请不要在字段上添加@XPath注解，组件只会对带有@XPath注解的字段进行反序列化