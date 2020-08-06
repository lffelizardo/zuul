package br.com.test.filter;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.stereotype.Component;

@Component
public class PostFilter extends ZuulFilter {
    Logger log = LoggerFactory.getLogger(PostFilter.class);
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.addZuulRequestHeader("Test", "TestSample");
        StringBuffer strLog=new StringBuffer();

        strLog.append("\n-- REQUEST RETURNED--\n");
        strLog.append(String.format("Request URL: %s Method: %s \n",ctx.getRequest().getRequestURL()
                ,ctx.getRequest().getMethod()));
        Enumeration<String> enume= ctx.getRequest().getHeaderNames();
        String header;
        while (enume.hasMoreElements())
        {
            header=enume.nextElement();
            strLog.append(String.format("Headers: %s = %s \n",header,ctx.getRequest().getHeader(header)));
        };
        strLog.append("Response code: "+ctx.getResponse().getStatus()+"\n");
        log.info(strLog.toString());
        return null;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public int filterOrder() {
        return 99;
    }

    @Override
    public String filterType() {
        return "post";
    }
}
