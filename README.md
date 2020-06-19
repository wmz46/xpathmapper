# xpathmapper
通过xpath注解对象的方式序列化以及反序列化xml

## 一、当前最新版本
```xml
<dependency>
  <groupId>com.iceolive</groupId>
  <artifactId>xpathmapper</artifactId>
  <version>1.1.0</version>
</dependency>
```
## 二、快速开始
### 举个例子，假如我们需要序列化反序列的xml格式如下
```xml
<input>
    <body>
        <student age="20" boarding="否">
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
    //布尔型如需特殊处理显示字符串，请设置trueString和falseString，默认为true/false
    @XPath(value ="/input/body/student/@boarding",trueString="是",falseString="否")
    private boolean boarding;
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
```java
//为避免&符号问题，组件会去除html中的script标签和注释
Student student = XPathMapper.parseHtml(html,Student.class);
```
### 5.注解说明
```java
    //value 表示字段的xpath，嵌套类的字段需使用相对路径./开头，非嵌套类的字段需使用完整路径/开头
    //当字段不是自定义对象也不是自定义对象的数组或列表，结尾必须是“text()”或“@属性名”，指明是取节点内容或属性值。
    //CDATA 表示内容是否需要CDATA标签包裹，默认为false
    //format 日期类型格式化用，无默认值。 如果是LocalDate类型，请注意不要写到时分秒。
    //trueString  布尔类型为真时的显示字符串，默认值true
    //falseString 布尔类型为假时的显示字符串，默认值false
    @XPath(value="/input/body/student/description/text()",CDATA = true,format="yyyy-MM-dd HH:mm:ss",trueString="是",falseString="否")
```
### 6.字段支持类型
目前字段支持类型

1.字符及字符串类型：String,char,Character

2.数值类型：byte,int,short,long,float,double,double,Byte,Integer,Short,Long,Float,Double,BigDecimal,支持数值类型的格式定义，可以根据格式规则序列化和反序列化带有格式的数值字符串。

数值格式化的定义请参考[标准数字格式字符串](https://docs.microsoft.com/zh-cn/dotnet/standard/base-types/standard-numeric-format-strings)和[自定义数字格式字符串](https://docs.microsoft.com/zh-cn/dotnet/standard/base-types/custom-numeric-format-strings),部分非常用格式可能未支持，详情请查看格式化组件项目[wmz46/stringutil](https://github.com/wmz46/stringutil)。

3.日期类型：Date,LocalDate,LocalDateTime,日期类型请给@XPath加上format,避免序列化反序列化异常。

4.布尔类型:boolean,Boolean，默认序列化字符串为true/false，如需自定义请设置@XPath的trueString和falseString。

5.以上4种类型的数组及列表，如String[],Set\<String>,List\<String>,HashSet\<String>,ArrayList\<String>，xpath的最后一个节点必须是text()或@属性名,倒数第二个节点为循环节点。

注：不支持循环节点不是倒数第二个节点的设置。
如xml结构如下，则不支持直接定义基础类型数组。因为循环节点为中间节点b，而不是最终节点c。
```xml
<a>
    <b><c>1</c></b>
    <b><c>2</c></b>
    <b><c>3</c></b>
</a>
```
上述结构只能通过增加一个嵌套类处理，如
```java
@Data
public class A{
    @XPath("/a/b")
    private List<B> bList;
}
@Data
public class B{
    @XPath("./c/text()")
    private int c;
}
```

6.自定义对象（必须有无参构造方法，否则会无法反序列化）

7.自定义对象（必须有无参构造方法，否则会无法反序列化）的数组及列表,如 YourClass[],Set\<YourClass>,List\<YourClass>,HashSet\<YourClass>,ArrayList\<YourClass>，xpath的最后一个节点为循环节点。


## 三、开发背景
在对接第三方xml报文接口时，有时对方提供的报文结构不太符合我们抽象出来的对象结构。通过JAXBContext虽然也可以实现需求，但可能需要写很多没有实际意义的中间嵌套类，而且赋值取值嵌套层次多的话，除了代码写起来多，还得判断中间嵌套层是否为null避免空指针异常，十分麻烦。
 
以上面的xml为例（为避免过于复杂化，此处忽略某些节点），假如我们用JAXBContext，需要定义如下类型
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
为了简化代码，更符合面向对象思想，故开发此组件。

## 四、存在的问题
### 命名空间
首先xpath由于不支持带有命名空间的查询，而且个人觉得xml的命名空间设计过于反人类。所以在反序列时，组件会自动去除xml字符串的命名空间。
### xpath高阶用法
目前只有反序列化的xpath支持带有谓语的路径表达式，比如/a[last()]/b[2]/c。而序列化只支持带数组序号的路径表达式，序号从1开始，如/a[1]/b[2]/c。
### 循环引用的序列化
目前组件没有循环引用检查，如果对象存在循环引用则会导致死循环。如果字段存在循环引用，请不要在字段上添加@XPath注解，组件只会对带有@XPath注解的字段进行反序列化
### 空字符串和null的问题
目前组件序列化，如果字段使用text()，当字段为null也会生成一个空节点。如果字段使用属性，当字段为null不会生成对应属性。

为什么要这么处理，原因有两点：1，保持xml节点结构完整，属性可丢弃。2，先生成节点后，给节点赋值处理起来比较简单(主要原因)。

而组件反序列时，如果字段使用text()或者属性时，遇到空节点会反序列成""空字符串，而不是null。

关于这个问题，我也不知道应该怎么处理，因为空节点你可以理解为null也可以理解成""空字符串。

可能这样处理看起来会有点别扭，毕竟xml不像json那样有null值。不过一般业务代码对字符串的判断，都是用StringUtils.isEmpty()来判空，问题不大。

