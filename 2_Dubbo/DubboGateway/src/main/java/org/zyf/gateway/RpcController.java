package org.zyf.gateway;

import com.alibaba.dubbo.config.annotation.Reference;
import org.dubbo.api.IHello;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zyf on 2018/4/16.
 */
@RestController
public class RpcController {

	@Reference
	private IHello hello;

	@RequestMapping("/")
	public String say(){
		return hello.say("gateway rpc");
	}

}
