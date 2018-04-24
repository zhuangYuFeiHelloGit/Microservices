package org.zyf.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.dubbo.api.IHello;

/**
 * Created by zyf on 2018/4/17.
 */
@Service//dubbo的@Service注解，用于暴露服务
public class SecondImpl implements IHello {
	@Override
	public String say(String s) {
		return "i am second hello";
	}
}
