package com.prm.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA fallback: 将所有非 API、非静态资源的请求转发到 index.html，
 * 解决 Vue History 路由刷新后 404/500 的问题。
 */
@Controller
public class SpaController {

    @RequestMapping(value = {"/{path:[^\\.]*}", "/{path:[^\\.]*}/**"})
    public String forward(HttpServletRequest request) {
        return "forward:/index.html";
    }
}
