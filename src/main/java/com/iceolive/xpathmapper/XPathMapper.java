package com.iceolive.xpathmapper;

import com.iceolive.xpathmapper.annotation.XPath;
import com.iceolive.xpathmapper.util.CollectionUtil;
import com.iceolive.xpathmapper.util.DateUtil;
import com.iceolive.xpathmapper.util.ReflectUtil;
import com.iceolive.xpathmapper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * xml工具类
 *
 * @author:wangmianzhe
 **/
@Slf4j
public class XPathMapper {


    private static <T> Object getValue(Document document, Class<T> clazz, Object obj, String prefix) {
        List<Field> fields = ReflectUtil.getAllFields(clazz);
        for (Field field : fields) {
            XPath xPath = field.getAnnotation(XPath.class);
            if (xPath == null) {
                continue;
            }
            String xPathStr = xPath.value();
            if (xPathStr.startsWith("./")) {
                xPathStr = prefix + xPathStr.substring(1);
            }
            //移除最后一个非节点
            if (xPathStr.endsWith("text()") || xPathStr.contains("@")) {
                xPathStr = xPathStr.substring(0, xPathStr.lastIndexOf("/"));
            }
            List<Node> nodes = document.selectNodes(xPathStr);
            if (CollectionUtil.isEmpty(nodes)) {
                continue;
            }
            boolean isList = false;
            boolean isArray = false;
            boolean isSet = false;
            Class<?> type = null;
            Object value = ReflectUtil.newInstance(field, nodes.size());
            if (field.getType().isArray()) {
                isArray = true;
                type = field.getType().getComponentType();
            } else if (field.getGenericType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                    type = (Class) parameterizedType.getActualTypeArguments()[0];
                    isList = true;
                } else if (Set.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                    type = (Class) parameterizedType.getActualTypeArguments()[0];
                    isSet = true;
                }
            } else {
                type = field.getType();
            }
            for (int i = 0; i < nodes.size(); i++) {
                String str = null;
                if (xPath.value().endsWith("text()")) {
                    str = nodes.get(i).getText();
                } else if (xPath.value().contains("@")) {
                    Attribute attribute = ((Element) nodes.get(i)).attribute(xPath.value().substring(xPath.value().lastIndexOf("@") + 1));
                    if (attribute != null) {
                        str = attribute.getStringValue();
                    }
                } else {
                    Object obj1;
                    if (i == 0) {
                        obj1 = ReflectUtil.newInstance(field, nodes.size());
                        ReflectUtil.setValue(obj, field.getName(), obj1);
                    } else {
                        obj1 = ReflectUtil.getValue(obj, field.getName());
                    }
                    if (isArray) {
                        Object obj2 = ReflectUtil.newInstance(type);
                        Array.set(obj1, i, obj2);
                        getValue(document, type, obj2, xPathStr + "[" + (i + 1) + "]");
                    } else if (isSet) {
                        Object obj2 = ReflectUtil.newInstance(type);
                        ((Set) obj1).add(obj2);
                        getValue(document, type, obj2, xPathStr + "[" + (i + 1) + "]");
                    } else if (isList) {
                        Object obj2 = ReflectUtil.newInstance(type);
                        ((List) obj1).add(obj2);
                        getValue(document, type, obj2, xPathStr + "[" + (i + 1) + "]");
                    } else {
                        getValue(document, type, obj1, xPathStr);
                    }
                }
                if (str != null) {
                    String typeName = type.getName();
                    Object val = getObject(xPath, type, str, typeName);
                    if (isArray) {
                        Array.set(value, i, val);
                    } else if (isSet) {
                        ((Set) value).add(val);
                    } else if (isList) {
                        ((List) value).add(val);
                    } else {
                        value = val;
                    }
                    ReflectUtil.setValue(obj, field.getName(), value);
                }
            }
        }
        return obj;
    }

    private static Object getObject(XPath xPath, Class<?> type, String str, String typeName) {
        Object val = null;
        //todo 支持更多类型
        if (StringUtil.isEmpty(str.trim())) {
            val = str;
        } else {
            if (typeName.equals("long") || typeName.equals("java.lang.Long")) {
                val = Long.parseLong(str);
            } else if (typeName.equals("int") || typeName.equals("java.lang.Integer")) {
                val = Integer.parseInt(str);
            } else if (typeName.equals("double") || typeName.equals("java.lang.Double")) {
                val = Double.parseDouble(str);
            } else if (typeName.equals("float") || typeName.equals("java.lang.Float")) {
                val = Float.parseFloat(str);
            } else if (typeName.equals("java.math.BigDecimal")) {
                val = new BigDecimal(str);
            } else if (typeName.equals("java.util.Date") || typeName.equals("java.time.LocalDateTime") || typeName.equals("java.time.LocalDate")) {
                if (!StringUtil.isEmpty(xPath.format())) {
                    val = DateUtil.parse(str, xPath.format(), type);
                }
            } else {
                val = str;
            }
        }
        return val;
    }


    public static <T> T parseHtml(String html, Class<T> clazz) {
        //去掉注释
        html = html.replaceAll("\\<!--(.+)--\\>", "");
        org.jsoup.nodes.Document document = Jsoup.parse(html);
        //处理未闭合标签
        document.outputSettings(new org.jsoup.nodes.Document.OutputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml));
        //去掉script，避免&字符
        document.getElementsByTag("script").remove();
        //只取html
        html = document.getElementsByTag("html").first().outerHtml();
        //处理html空格，理论上有其他和xml不兼容的也要加
        html = "<!DOCTYPE html [\n" +
                "<!ENTITY nbsp \"&#160;\"> \n" +
                "]>" + html;
        return parse(html, clazz);
    }

    public static <T> T parse(String xml, Class<T> clazz) {
        try {
            //去掉xmlns
            xml = xml.replaceAll("xmlns=\"(.+)\"", "");
            Document document = DocumentHelper.parseText(xml);

            T obj = ReflectUtil.newInstance(clazz);
            getValue(document, clazz, obj, "");
            return obj;
        } catch (DocumentException e) {
            log.error("xml反序列化异常", e);
            throw new RuntimeException(e);
        }
    }

    public static String format(Object obj, boolean suppressDeclaration) {
        try {
            Document document = DocumentHelper.createDocument();
            if (document == null) {
                throw new RuntimeException("文档不能为空");
            }
            setValue(document, null, obj);
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            // 设置XML编码方式,即是用指定的编码方式保存XML文档到字符串(String),这里也可以指定为GBK或是ISO8859-1  
            outputFormat.setEncoding("UTF-8");
            //是否生产xml头
            outputFormat.setSuppressDeclaration(suppressDeclaration);
            //设置是否缩进
            outputFormat.setIndent(true);
            //以四个空格方式实现缩进
            outputFormat.setIndent("    ");
            //设置是否换行
            outputFormat.setNewlines(true);
            StringWriter stringWriter = new StringWriter();
            XMLWriter xmlWriter = new XMLWriter(stringWriter, outputFormat);
            xmlWriter.write(document);
            return stringWriter.toString();
        } catch (IOException e) {
            log.error("xml序列化异常", e);
            throw new RuntimeException(e);
        }
    }

    private static void setValue(Document document, Element element, Object value) {
        Class clazz = value.getClass();
        List<Field> fields = ReflectUtil.getAllFields(clazz);
        Element root = element;
        for (Field field : fields) {
            element = root;
            XPath xPath = field.getAnnotation(XPath.class);
            if (xPath != null) {
                String[] nodes;
                if (xPath.value().startsWith("./")) {
                    nodes = xPath.value().substring(2).split("/");
                } else if (xPath.value().startsWith("/")) {
                    //如果是绝对路径，则清除element
                    element = null;
                    nodes = xPath.value().substring(1).split("/");
                } else {
                    throw new RuntimeException("不支持的xpath");
                }
                for (int i = 0; i < nodes.length; i++) {
                    String node = nodes[i];
                    if (i == nodes.length - 1) {
                        Object val = ReflectUtil.getValue(value, field.getName(), clazz);
                        Object[] values;
                        if (val == null) {

                        } else if (val instanceof Set) {
                            if (((Set) val).isEmpty()) {
                                return;
                            }
                            values = ((Set) val).toArray();
                            setValue(document, element, values, node, xPath);
                        } else if (val instanceof List) {
                            if (((List) val).isEmpty()) {
                                return;
                            }
                            values = ((List) val).toArray();
                            setValue(document, element, values, node, xPath);
                        } else if (val.getClass().isArray()) {
                            int length = Array.getLength(val);
                            if (length == 0) {
                                return;
                            }
                            values = new Object[length];
                            for (int j = 0; j < length; j++) {
                                values[j] = Array.get(val, j);
                            }
                            setValue(document, element, values, node, xPath);
                        } else {
                            String str = val.toString();
                            if (!StringUtil.isEmpty(xPath.format())) {
                                if (val instanceof Date) {
                                    str = DateUtil.format(((Date) val), xPath.format());
                                } else if (val instanceof LocalDate) {
                                    str = DateUtil.format(((LocalDate) val), xPath.format());
                                } else if (val instanceof LocalDateTime) {
                                    str = DateUtil.format(((LocalDateTime) val), xPath.format());
                                }
                            }
                            if (node.startsWith("@")) {
                                element.addAttribute(node.substring(1), str);
                            } else if (node.equals("text()")) {
                                if (xPath.CDATA()) {
                                    element.addCDATA(str);
                                } else {
                                    element.setText(str);
                                }
                            } else {
                                //处理对象
                                element = element.addElement(node);
                                setValue(document, element, val);
                            }
                        }
                    } else {
                        if (element == null) {
                            if (document.getRootElement() == null) {
                                element = document.addElement(node);
                            } else {
                                element = document.getRootElement();
                            }
                        } else if (element.element(node) == null) {
                            element = element.addElement(node);
                        } else {
                            element = element.element(node);
                        }
                    }
                }

            }
        }
    }


    private static void setValue(Document document, Element element, Object[] values, String node, XPath xPath) {
        Element parent = element.getParent();
        Element currentElement = element;
        for (int j = 0; j < values.length; j++) {
            if (node.startsWith("@") || node.equals("text()")) {
                if (j > 0) {
                    if (parent.elements(element.getName()).size() <= j) {
                        element = parent.addElement(element.getName());
                    } else {
                        element = parent.elements(element.getName()).get(j);
                    }
                }
            }
            String str = values[j].toString();
            if (!StringUtil.isEmpty(xPath.format())) {
                if (values[j] instanceof Date) {
                    str = DateUtil.format(((Date) values[j]), xPath.format());
                } else if (values[j] instanceof LocalDate) {
                    str = DateUtil.format(((LocalDate) values[j]), xPath.format());
                } else if (values[j] instanceof LocalDateTime) {
                    str = DateUtil.format(((LocalDateTime) values[j]), xPath.format());
                }
            }
            if (node.startsWith("@")) {
                element.addAttribute(node.substring(1), str);
            } else if (node.equals("text()")) {
                if (xPath.CDATA()) {
                    element.addCDATA(str);
                } else {
                    element.setText(str);
                }
            } else {
                if (currentElement.elements(node).size() > j) {
                    element = currentElement.elements(node).get(j);
                } else {
                    element = currentElement.addElement(node);
                }
                setValue(document, element, values[j]);
            }
        }
    }
}
