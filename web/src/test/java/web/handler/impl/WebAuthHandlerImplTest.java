package web.handler.impl;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import web.AuthWebVerticle;

import java.util.List;

/**
 * Created by liulaoye on 17-6-13.
 * test
 */
@RunWith(VertxUnitRunner.class)
public class WebAuthHandlerImplTest{
    private static Vertx vertx;
    private static HttpClient httpClient;
    private static final int PORT = 8000;

    @BeforeClass
    public static void setUp( TestContext context ){

        vertx = Vertx.vertx();
        HttpClientOptions options = new HttpClientOptions();
        options.setConnectTimeout( 7000 );
        options.setDefaultPort( PORT );
        options.setDefaultHost( "localhost" );
        httpClient = vertx.createHttpClient( options );

        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances( 1 );

        vertx.deployVerticle( AuthWebVerticle.class.getName(), deploymentOptions, context.asyncAssertSuccess() );
    }

    private static class SessionCookie{
        List<String> get(){
            return cookies;
        }

        void set( List<String> cookies ){
            this.cookies = cookies;
        }

        List<String> cookies;
    }

    @AfterClass
    public static void tearDown( TestContext context ){
        vertx.close( context.asyncAssertSuccess() );
    }

    @Test
    public void testLogin( TestContext context ) throws Exception{
        httpClient.getNow( "/login?username=lk&&password=lk", res -> {//登录
            context.assertEquals( 200, res.statusCode() );
        });
    }
    @Test
    public void handle( TestContext context ) throws Exception{
        final Async async = context.async();


        final SessionCookie sessionCookie = new SessionCookie();
        httpClient.getNow( "/api/product/add", res ->//尚未登录
                context.assertEquals( 403, res.statusCode() ) );

        httpClient.getNow( "/login?username=lk&&password=lk", res -> {//登录
            context.assertEquals( 200, res.statusCode() );
            sessionCookie.set( res.cookies() );
            res.bodyHandler( body -> context.assertEquals( true, body.toString().contains( "sys" ) ) );

            sendRequestWithCookie( sessionCookie.get(), "/api/product/del", res1 -> context.assertEquals( 200, res1.statusCode() ) );//正常访问

            sendRequestWithCookie( sessionCookie.get(), "/api/product/add", res1 -> {//即使登录也无权限
                context.assertEquals( 403, res1.statusCode() );
                async.complete();
            } );

        } );


    }

    private void sendRequestWithCookie( List<String> cookies,  String uri, Handler<HttpClientResponse> responseHandler ){
        final HttpClientRequest req = httpClient.get( uri, responseHandler );
        if( cookies != null ) {
            req.putHeader( HttpHeaders.COOKIE.toString(), cookies );
        }
        req.end();
    }


}