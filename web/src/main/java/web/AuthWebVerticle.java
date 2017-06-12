package web;

import auth.CustomJdbcAuth;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.handler.WebAuthHandler;
import web.handler.impl.ProductHandler;


/**
 * Created by liulaoye on 17-6-8.
 * 带自定义的Auth的web演示
 */

public class AuthWebVerticle extends AbstractVerticle{
    private static final int PORT = 8000;
    private CustomJdbcAuth authProvider;
    private static final Logger logger = LoggerFactory.getLogger( AuthWebVerticle.class.getName() );

    @Override
    public void start() throws Exception{


//        super.start();
        init();

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router( vertx );

        router.route().handler( CookieHandler.create() );
        router.route().handler( SessionHandler.create( LocalSessionStore.create( vertx ) ) );

        router.route().handler( UserSessionHandler.create( authProvider ) );
        router.route().handler( WebAuthHandler.create( authProvider ) );

//        AuthHandler basicAuthHandler = BasicAuthHandler.create( authProvider );
////        basicAuthHandler.addAuthority( "abcd" );
//
//        router.route( "/api/*" ).handler( basicAuthHandler );
//        router.route( "/api/*" ).handler( ctx -> {
//            final CustomWebUser user = (CustomWebUser) ctx.user();
//
//
//            ctx.response().end( user.getRoles().toString() );
//        } );
        dispatcher( router );
//        final Handler<RoutingContext> product = this::product;


//        final Handler<RoutingContext> test = this::test;

//        router.route( "/public" ).handler( this::product );
//        System.out.println( test );
//        System.out.println( "test instanceof Handler is " + (test instanceof Handler) );

        server.requestHandler( router::accept ).listen( PORT );
        logger.info("init end");

    }





    private void dispatcher( Router mainRouter ){
        Router restAPI = Router.router( vertx );

        mainRouter.mountSubRouter("/api/product", new ProductHandler().addRouter( restAPI ) );


    }

    /**
     * 初始化jdbcClient以及authProvider
     */

    private void init(){

        JsonObject jdbcClientConfig = new JsonObject()
                .put( "provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider" )
                .put( "jdbcUrl", "jdbc:mysql://localhost:3306/dlb?useSSL=true" )
                .put( "username", "root" )
                .put( "password", "root" )
//                .put("driverClassName", "org.postgresql.Driver")
                .put( "maximumPoolSize", 30 );
        JDBCClient jdbcClient = JDBCClient.createShared( vertx, jdbcClientConfig );
        authProvider = new CustomJdbcAuth( vertx, jdbcClient );


//        this.config()
    }


    public static void main( String[] args ){
        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions options = new DeploymentOptions();
        options.setInstances( 10 );

        options.setConfig( new JsonObject().put( "/private/product/add", "admin" ) );
        vertx.deployVerticle( AuthWebVerticle.class.getName(), options, res -> {
            if( res.succeeded() ) {
                System.out.println( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
            }else{
                res.cause().printStackTrace();
            }
        } );

    }
}
