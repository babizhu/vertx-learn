package web;

import anno.RequirePermission;
import auth.CustomJdbcAuth;
import auth.CustomWebUser;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.ContextTask;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Created by liulaoye on 17-6-8.
 * 带自定义的Auth的web演示
 */
public class AuthWebVerticle extends AbstractVerticle{
    private static final int PORT = 8000;
    private CustomJdbcAuth authProvider;

    @Override
    public void start() throws Exception{


//        super.start();
        init();

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router( vertx );

        router.route().handler( CookieHandler.create() );
        router.route().handler( SessionHandler.create( LocalSessionStore.create( vertx ) ) );

        router.route().handler( UserSessionHandler.create( authProvider ) );

        AuthHandler basicAuthHandler = BasicAuthHandler.create( authProvider );
//        basicAuthHandler.addAuthority( "abcd" );

        router.route( "/api/*" ).handler( basicAuthHandler );
        router.route( "/api/*" ).handler( ctx -> {
            final CustomWebUser user = (CustomWebUser) ctx.user();


            ctx.response().end( user.getRoles().toString() );
        } );
        final Handler<RoutingContext> product = this::product;
        final Handler<RoutingContext> test = this::test;

        router.route( "/public" ).handler( product );
        System.out.println( test );
        System.out.println( "test instanceof Handler is " + (test instanceof Handler) );

        server.requestHandler( router::accept ).listen( PORT );

    }

    private void test( RoutingContext ctx ){

    }

    @RequirePermission( "/product/list" )
    private void product( RoutingContext ctx ){
        ctx.response().end( "product111" );
    }

    /**
     * 初始化jdbcClient以及authProvider
     */
    private void init(){

        final Method[] methods = this.getClass().getDeclaredMethods();
        for( Method method : methods ) {
            System.out.println( method.getName() + (method.getDeclaredAnnotations()) );
            if( method.isAnnotationPresent( RequirePermission.class ) ) {
                System.out.println(method.getDeclaredAnnotation(RequirePermission.class  ).value());
            }
        }

//        System.out.println(config().getString("/private/product/add"));
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
//        options.setInstances( 1 );

        options.setConfig( new JsonObject().put( "/private/product/add", "admin" ) );
        vertx.deployVerticle( AuthWebVerticle.class.getName(), options, res -> {
            if( res.succeeded() ) {
                System.out.println( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
            }
        } );

    }
}
