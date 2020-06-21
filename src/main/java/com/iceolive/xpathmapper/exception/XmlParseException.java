package com.iceolive.xpathmapper.exception;

/**
 * Xml反序列异常
 *
 * @author:wangmianzhe
 **/
public class XmlParseException extends RuntimeException {
    public XmlParseException(){
        super("Xml反序列异常");
    }
    public XmlParseException( Throwable cause) {
        super("Xml反序列异常", cause);
    }
}
