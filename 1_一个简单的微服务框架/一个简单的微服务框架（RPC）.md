### 一个简单的微服务框架（RPC）
参考书籍：**微服务分布式构架开发实战 龚鹏**

RPC：Remote Procedure Call ---远程过程调用。简单说就是通过http协议，连接两个应用程序，应用程序之间通过接口实现数据共享。

**基本工作流程：**

* 客户端发起调用请求（调用服务端的某一个方法）
* 将调用的内容序列化后通过网络发给服务端（序列化数据，方便在流中传输对象）（是通过socket发送数据的）
* 服务端接收到调用请求，执行具体服务并获得结果
* 将结果序列化后通过网络返回给客户端

#### 1，公共接口的定义
在发起远程调用时，需要基于接口（interface）来约定，客户端与服务端所调用服务的具体内容。

* 新建maven工程，maven参数如下
![](https://ws1.sinaimg.cn/large/006tKfTcgy1fpunnttyc4j30my04ewez.jpg)

* 定义接口 `HelloService`

```java
package org.zyf.rpc;

/**
 * Created by zyf on 2018/3/30.
 */
public interface HelloService {

	String hello(String name);

}
```

* 生成jar包，方便其他应用导入依赖
![](https://ws1.sinaimg.cn/large/006tKfTcgy1fpunqy50lgj308s06p3zf.jpg)

#### 2，服务端
* 新建用于提供服务端Maven应用，并在 `pom.xml` 中引入刚编写好的接口应用的依赖

![](https://ws1.sinaimg.cn/large/006tKfTcgy1fpunt3crp8j30a404pwfk.jpg)

* 创建 `HelloService` 接口的实现类 `HelloServiceImpl` ，该类用来处理业务请求

```java
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

```

* 编写监听服务类 `Server` ，该类用来操作服务端，开启服务，注册业务等操作

```java
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

```

* 创建服务端程序入口类 `App` ，提供main方法，创建并使用Server对象

```java
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
```

#### 3，客户端
* 新建用于提供客户端Maven应用，并在 `pom.xml` 中引入刚编写好的接口应用的依赖

![](https://ws4.sinaimg.cn/large/006tKfTcgy1fpuo6syyaoj30b805rgmt.jpg)

* 编写远程调用类，想服务端发起Socket远程调用请求，并获得服务端的反馈信息

```java

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

```

* 创建客户端程序入口类 `App` ，提供main方法，创建并使用Server对象

```java
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

```


#### 4，运行演示：先启动服务端，再启动客户端

* 启动服务端
![](https://ws2.sinaimg.cn/large/006tKfTcgy1fpuo9yuyxjj30wo03owew.jpg)

* 启动客户端后
![](https://ws1.sinaimg.cn/large/006tKfTcgy1fpuoarf807j30w6046aan.jpg)
![](https://ws2.sinaimg.cn/large/006tKfTcgy1fpuob139u2j30ff028wen.jpg)




#### 5，程序流程总结
![](https://ws1.sinaimg.cn/large/006tKfTcgy1fpuozetd3gj311m0tu7s1.jpg)



