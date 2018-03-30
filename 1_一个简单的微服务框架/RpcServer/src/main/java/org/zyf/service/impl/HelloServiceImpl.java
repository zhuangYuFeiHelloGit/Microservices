package org.zyf.service.impl;

import org.zyf.rpc.HelloService;

/**
 * Created by zyf on 2018/3/30.
 */
public class HelloServiceImpl implements HelloService {
	public String hello(String s) {
		System.out.println("接收到消息："+s);
		return "你好："+s;
	}
}
