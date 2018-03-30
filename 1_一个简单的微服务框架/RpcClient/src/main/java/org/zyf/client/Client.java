package org.zyf.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by zyf on 2018/3/30.
 */
public class Client<T> {

	/**	这里使用的是javajdk的动态代理
	 * 根据提供的服务接口类，将接口序列化成码流
	 * 向目标服务端发起Socket远程请求
	 * 获得服务器反馈的结果，并反序列化对象后返回
	 * @param serviceInterface
	 * @param addr
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(final Class<?> serviceInterface, final InetSocketAddress addr){


		//提供接口的类加载器
		//提供接口的类对象
		//得到一个实现该接口的代理类
		final T instance = (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				Socket socket = null;
				ObjectOutputStream output = null;
				ObjectInputStream input = null;

				try{
					//连接服务端
					socket = new Socket();
					//连接端口地址
					socket.connect(addr);

					//将调用的接口类，方法名，参数列表等序列化后方给服务提供者
					output = new ObjectOutputStream(socket.getOutputStream());

					//直接根据UTF-8编码输出接口名
					output.writeUTF(serviceInterface.getName());

					//直接根据UTF-8编码输出方法名
					output.writeUTF(method.getName());

					output.writeObject(method.getParameterTypes());

					output.writeObject(args);

					input = new ObjectInputStream(socket.getInputStream());

					return input.readObject();
				}finally {
					if(socket != null) socket.close();
					if(output != null) output.close();
					if(input != null) input.close();
				}
			}
		});


		return instance;
	}
}
