package Outlet;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/ProgrammingFiles/*")
public class ProgrammingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        User u = Conn.getUser(request);
        if(u!=null && Conn.isLoggedIn(u.token) && u.team!=null &&u.tid>0 && u.team.start > 0 && Submit.TIME_LIMIT - (System.currentTimeMillis() - u.team.start) > 0) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            response.sendRedirect(request.getContextPath());
        }
    }
}