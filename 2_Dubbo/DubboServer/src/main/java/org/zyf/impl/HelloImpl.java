package org.zyf.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.dubbo.api.IHello;

/**
 * Created by zyf on 2018/4/2.
 */
@Service
public class HelloImpl implements IHello{
	@Override
	public String say(String s) {
		System.out.printf("说点什么："+s);
		return s;
	}
}
