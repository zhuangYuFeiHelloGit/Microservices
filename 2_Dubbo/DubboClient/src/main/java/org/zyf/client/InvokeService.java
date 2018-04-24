package org.zyf.client;

import com.alibaba.dubbo.config.annotation.Reference;
import org.dubbo.api.IHello;
import org.springframework.stereotype.Component;

/**
 * Created by zyf on 2018/4/2.
 */
@Component
public class InvokeService {

	@Reference//dubbo的此注解，用于生产远程服务代理
	public IHello iHello;
}
