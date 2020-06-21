package com.iceolive.xpathmapper.exception;

/**
 * Xml序列化异常
 *
 * @author:wangmianzhe
 **/
public class XmlFormatException extends RuntimeException {
    public XmlFormatException(){
        super("xml序列化异常");
    }
    public XmlFormatException(Throwable cause) {
        super("xml序列化异常", cause);
    }
}
