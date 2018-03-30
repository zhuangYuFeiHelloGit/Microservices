package org.zyf;

import org.zyf.rpc.HelloService;
import org.zyf.server.Server;
import org.zyf.service.impl.HelloServiceImpl;

import java.io.IOException;

/**
 * Created by zyf on 2018/3/30.
 */
public class App {

	public static void main(String[] args) throws IOException {
		Server server = new Server();
		//注册服务
		server.register(HelloService.class, HelloServiceImpl.class);
		//启动并绑定端口
		server.start(9999);

	}

}
