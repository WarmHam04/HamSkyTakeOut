package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        //可直接修改yml文件中的AdminTokenName的值
        //String token = request.getHeader("Authorization");
        String authHeader = request.getHeader(jwtProperties.getAdminTokenName());
        if(authHeader!=null&&authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            //System.out.println("后端接收到的token: " + token);

            //2、校验令牌
            try {
                log.info("jwt校验:{}", token);
                Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
                //获取当前员工id
                Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
                log.info("当前员工id：", empId);

                //以当前的员工的id来设置一个线程id
                BaseContext.setCurrentId(empId);

                //3、通过，放行
                return true;
            } catch (Exception ex) {
                //4、不通过，响应401状态码
                response.setStatus(401);
                return false;
            }
        }else log.info("后端接受到的authHeader不符合要求:{}", authHeader);


        return false;




    }
}
