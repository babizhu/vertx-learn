package demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by liulaoye on 17-5-15.
 * web服务器
 */
public class HttpServerDemo1
        extends AbstractVerticle{

    @Override
    public void start() throws Exception{
        super.start();



        String host = config().getString("api.gateway.http.address", "localhost");
        int port = config().getInteger("api.gateway.http.port", 8000); // (1)
        Router router = Router.router(vertx); // (2)
        router.route().handler( this::auth );
        // api dispatcher
        router.route("/api/*").handler(this::dispatchRequests); // (10)
//        router.route("/*").handler(StaticHandler.create()); // (12)
        router.route("/*").handler(this::staticFile); // (12)


        // create http server
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> { // (14)
                    if (ar.succeeded()) {
//                        publishApiGateway(host, port);
//                        future.complete();
//                        logger.info("API Gateway is running on port " + port);
//                        // publish log
//                        publishGatewayLog("api_gateway_init_success:" + port);
                    } else {
//                        future.fail(ar.cause());
                    }
                });

    }

    private void auth( RoutingContext ctx ){
        String res = ctx.request().method().name() + " " + ctx.request().uri();
        final User user = ctx.user();
        ctx.response().end(res);
    }

    private void staticFile( RoutingContext routingContext ){
        routingContext.response().end("staticFile");
        System.out.println( "HttpServerDemo1.staticFile" );
    }

    private void dispatchRequests( RoutingContext routingContext ){
        routingContext.response().end("api");
        System.out.println( "HttpServerDemo1.dispatchRequests" );
    }

    public static void main( String[] args ){
        Vertx vertx = Vertx.vertx();


        vertx.deployVerticle( HttpServerDemo1.class.getName() );
    }
}
