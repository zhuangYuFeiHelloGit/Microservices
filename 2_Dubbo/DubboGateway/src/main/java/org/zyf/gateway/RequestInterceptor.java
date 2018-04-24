package org.zyf.gateway;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zyf on 2018/4/16.
 */
public class RequestInterceptor implements HandlerInterceptor {

	/**
	 * 在请求处理前执行，用于权限验证，参数过滤等
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @param o
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
		String token = httpServletRequest.getParameter("token");
		if(token != null && token.equals("1")){
			//为true，则执行下一个拦截器，若所以拦截器都执行完，则执行被拦截的Controller
			return true;
		}

		httpServletResponse.getWriter().write("token error");
		//不再执行后续的拦截器链及被拦截的Controller
		return false;
	}

	/**
	 * 当前请求进行处理之后执行，主要用于日志记录，权限检查，性能监控，通用行为等
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @param o
	 * @param modelAndView
	 * @throws Exception
	 */
	@Override
	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

	}

	/**
	 * preHandle 返回值为true时执行，主要用于资源清理工作
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @param o
	 * @param e
	 * @throws Exception
	 */
	@Override
	public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

	}
}
