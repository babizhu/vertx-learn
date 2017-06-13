package web.handler.impl;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
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
    final HttpClient httpClient = vertx.createHttpClient();
    @BeforeClass
    public static void setUp( TestContext context ){

        vertx = Vertx.vertx();


        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions options = new DeploymentOptions();
        options.setInstances( 1 );

        vertx.deployVerticle( AuthWebVerticle.class.getName(), options, context.asyncAssertSuccess() );
    }

    private static class SessionCookie{
        public List<String> get(){
            return cookies;
        }

        public void set( List<String> cookies ){
            this.cookies = cookies;
        }

        List<String> cookies;
    }

    @AfterClass
    public static void tearDown( TestContext context ){
        vertx.close( context.asyncAssertSuccess() );
    }

    @Test
    public void handle( TestContext context ) throws Exception{
        final Async async = context.async();


        int PORT = 8000;
        final SessionCookie sessionCookie = new SessionCookie();
        httpClient.getNow( PORT, "localhost", "/api/product/add", res ->
                context.assertEquals( 403, res.statusCode() ) );

        httpClient.getNow( PORT, "localhost", "/login?username=lk&&password=lk", res -> {
            context.assertEquals( 200, res.statusCode() );
            sessionCookie.set( res.cookies() );
            res.bodyHandler( body -> context.assertEquals( true, body.toString().contains( "sys" ) ) );
            sendRequestWithCookie(sessionCookie.get(),PORT,"localhost","/api/product/del",res1 -> {
                context.assertEquals( 200, res1.statusCode() );
                async.complete();

            } );

        } );


//        final HttpClientRequest req = httpClient.get( PORT, "localhost", "/api/product/add", res -> {
//            context.assertEquals( 403, res.statusCode() );
//
//        } );
//        if( sessionCookie.get() != null ) {
//            req.putHeader( "cookie", sessionCookie.get() );
//        }



    }
    private void sendRequestWithCookie( List<String> cookies,int port,String host,String uri,Handler<HttpClientResponse> responseHandler){
        final HttpClientRequest req = httpClient.get( port, host, uri, responseHandler);
        if( cookies != null ) {
            req.putHeader( HttpHeaders.SET_COOKIE.toString(), cookies );
        }
        req.end();
    }


}