package org.zyf.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zyf on 2018/3/30.
 */
public class Server {
	private static ExecutorService executor = Executors.newFixedThreadPool(10);

	//此hashMap用来保管定义的服务接口与用到的对应的实现类
	private static final HashMap<String,Class> serviceRegistry = new HashMap<String, Class>();

	/**
	 * 对外暴露的，用于注册服务接口与对应实现类的方法
	 * @param serviceInterface
	 * @param impl
	 */
	public void register(Class serviceInterface,Class impl){
		//注册服务
		//将接口名与实现类名存入hashMap中
		serviceRegistry.put(serviceInterface.getName(),impl);
	}

	/**
	 * 启动一个阻塞式的Socket服务，用于等待客户端发起的调用请求
	 * 当收到请求后，将码流反序列化成对象
	 * 并根据接口，从注册列表（serviceRegistry）中寻找具体实现类
	 * 最终通过反射的方式调用该实现类的方法，得到方法结果
	 * @param port
	 * @throws IOException
	 */
	public void start(int port) throws IOException {
		final ServerSocket server = new ServerSocket();

		//绑定监听端口
		server.bind(new InetSocketAddress(port));

		System.out.println("======服务已启动======");

		while (true){
			executor.execute(new Runnable() {
				public void run() {
					Socket socket = null;
					ObjectInputStream input = null;
					ObjectOutputStream output = null;
					try {

						//获取到server连接的客户端
						socket = server.accept();

						//获得socket的输入流，并作为构造方法参数，得到对象输入流
						input = new ObjectInputStream(socket.getInputStream());

						//反序列化定位到具体的服务
						String serviceName = input.readUTF();

						//简单的看了下readUTF的源码，大致是一次性能读出一段数据
						//而serviceName和methodName就相当于两段数据
						String methodName = input.readUTF();

						Class<?>[] parameterTypes= (Class<?>[]) input.readObject();

						Object[] arguments = (Object[]) input.readObject();

						//在服务注册表中，根据调用的服务获取到具体的实现类
						Class serviceClass = serviceRegistry.get(serviceName);
						if(serviceClass == null){
							throw new ClassNotFoundException(serviceName+" 未找到");
						}

						//反射获得该类中的方法
						Method method = serviceClass.getMethod(methodName,parameterTypes);

						//调用方法，获得方法返回值
						Object result = method.invoke(serviceClass.newInstance(), arguments);

						//得到对象输出流
						output = new ObjectOutputStream(socket.getOutputStream());

						//将结果序列化发送给客户端
						output.writeObject(result);

					}catch (Exception e){
						e.printStackTrace();
					}finally {
						//关闭资源
						try {
							if(socket != null) socket.close();
							if (input != null) input.close();
							if (output != null) output.close();
						}catch (IOException e){
							e.printStackTrace();
						}
					}
				}
			});
		}


	}
}
