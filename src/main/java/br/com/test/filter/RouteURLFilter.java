package br.com.test.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;

import br.com.test.request.GatewayRequest;
import br.com.test.request.URIRequest;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

@Component
public class RouteURLFilter extends ZuulFilter {


    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER+1;

    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if ( ctx.getRequest().getRequestURI() == null || ! ctx.getRequest().getRequestURI().startsWith("/url"))
            return false;
        return ctx.getRouteHost() != null
                && ctx.sendZuulResponse();
    }

    @Override
    public Object run() {
        try {

            RequestContext ctx = RequestContext.getCurrentContext();


            URIRequest uriRequest;

            try  {
                uriRequest= getURIRedirection(ctx);
            } catch (ParseException k)
            {
                ctx.setResponseBody(k.getMessage());
                ctx.setResponseStatusCode(HttpStatus.BAD_REQUEST.value());
                ctx.setSendZuulResponse(false);
                return null;
            }

            UriComponentsBuilder uriComponent=UriComponentsBuilder.fromHttpUrl(uriRequest.getUrl() );
            if (uriRequest.getPath()==null)
                uriRequest.setPath("/");
            uriComponent.path(uriRequest.getPath());

            String uri=uriComponent.build().toUriString();
            ctx.setRouteHost(new URL(uri));
        } catch (IOException k)
        {
            k.printStackTrace();
        }
        return null;
    }

    URIRequest getURIRedirection(RequestContext ctx) throws ParseException
    {
        URIRequest uriRequest;
        String hostDestino=ctx.getRequest().getHeader("hostTarget");
        if ( hostDestino==null)
        {
            uriRequest=getURIFromBody(ctx);
            if (uriRequest.getUrl()!=null)
                return uriRequest;
            throw new ParseException("Variable 'hostTarget' must be set as HEADER variable",0);
        }

        String pathDestino=ctx.getRequest().getHeader("hostTarget");

        uriRequest=new URIRequest();
        uriRequest.setUrl(hostDestino);
        uriRequest.setPath(pathDestino);

        return uriRequest;
    }

    URIRequest getURIFromBody(RequestContext ctx)  throws ParseException
    {
        Charset utf8=Charset.forName("UTF-8");
        URIRequest uriRequest=new URIRequest();
        try {
            InputStream in = (InputStream) ctx.get("requestEntity");
            if (in == null) {
                in = ctx.getRequest().getInputStream();
            }
            String body = StreamUtils.copyToString(in, utf8);
            if (body!=null)
            {

                ObjectMapper map = new ObjectMapper();

                map.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                try
                {
                    GatewayRequest gatewayRequest =map.readValue(body, GatewayRequest.class);
                    if (gatewayRequest!=null)
                    {
                        uriRequest=gatewayRequest.getUri();
                        if (uriRequest == null || uriRequest.getUrl()==null)
                            throw new ParseException("The 'URL' variable of the body in the URI object must have a value",0);
                        body=gatewayRequest.getBody();
                        uriRequest.setBody(body.getBytes(utf8));
                    }
                } catch (IOException  k1)
                {
                }
                ctx.set("requestEntity", new ByteArrayInputStream(body.getBytes(utf8)));

                return uriRequest;
            }
        } catch (IOException k1)
        {
            k1.printStackTrace();
        }
        return uriRequest;
    }
}
