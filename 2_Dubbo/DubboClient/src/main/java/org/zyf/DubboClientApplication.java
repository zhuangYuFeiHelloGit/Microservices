package org.zyf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.zyf.client.InvokeService;

@SpringBootApplication
public class DubboClientApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run =
				SpringApplication.run(DubboClientApplication.class, args);

		InvokeService invokeService = run.getBean(InvokeService.class);
		System.out.println("收到返回结果："+invokeService.iHello.say("rpc"));
	}
}
