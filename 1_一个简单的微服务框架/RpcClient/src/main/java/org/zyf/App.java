package org.zyf;

import org.zyf.client.Client;
import org.zyf.rpc.HelloService;

import java.net.InetSocketAddress;

/**
 * Created by zyf on 2018/3/30.
 */
public class App {
	public static void main(String[] args) {
		HelloService service = Client.get(HelloService.class,new InetSocketAddress("localhost",9999));

		System.out.println(service.hello("RPC"));
	}
}
