package com.iceolive.xpathmapper.exception;

/**
 * 不支持的xpath
 *
 * @author:wangmianzhe
 **/
public class UnsupportedXPathException extends RuntimeException {
    public UnsupportedXPathException(String xPath) {
        super("不支持的XPath：" + xPath);
    }

    public UnsupportedXPathException() {
        super("不支持的XPath");
    }
}
