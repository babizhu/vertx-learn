package web;

import auth.CustomJdbcAuth;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
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
    private static final Logger         logger = LoggerFactory.getLogger( AuthWebVerticle.class.getName() );
    private static final int            PORT = 8000;
    public static final String          API_PREFIX = "/api/";
    private JDBCClient                  jdbcClient;
    private CustomJdbcAuth              authProvider;

    @Override
    public void start(){

        initJdbcAuth();

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router( vertx );

        router.route().handler( CookieHandler.create() );
        router.route().handler( SessionHandler.create( LocalSessionStore.create( vertx ) ) );
        router.route().handler( UserSessionHandler.create( authProvider ) );

        router.route( "/login" ).handler( ( RoutingContext ctx ) -> {
            if( ctx.user() != null ) {
                ctx.response().end( "不要重复登录！！！" );
                return;
            }
            String userName = ctx.request().getParam( "userName" );
            String password = ctx.request().getParam( "password" );
            if( userName == null || userName.isEmpty() ) {
                userName = "tim";
            }
            if( password == null || password.isEmpty() ) {
                password = "sausages";
            }


            JsonObject authInfo = new JsonObject().put( "username", userName ).put( "password", password );
            authProvider.authenticate( authInfo, res -> {
                if( res.succeeded() ) {
                    User user = res.result();
                    ctx.setUser( user );
                    Session session = ctx.session();
                    if( session != null ) {
                        // the user has upgraded from unauthenticated to authenticated
                        // session should be upgraded as recommended by owasp
                        session.regenerateId();
                        ctx.response().end( user + "验证成功！！！\n" );
                    } else {
                        ctx.fail( 403 );
                    }
                } else {
                    ctx.response().end( res.cause().toString() );
                }
            } );
        } );


        router.route( API_PREFIX + "*" ).handler( WebAuthHandler.create( authProvider ) ).failureHandler( this::failur );
        dispatcher( router, jdbcClient );
        server.requestHandler( router::accept ).listen( PORT );

    }

    private void failur( RoutingContext ctx ){
//        ctx.
        System.out.println(ctx.failure().getCause());
    }


    private void dispatcher( Router mainRouter, JDBCClient jdbcClient ){
        Router restAPI = Router.router( vertx );
        mainRouter.mountSubRouter( API_PREFIX + "product", new ProductHandler(jdbcClient).addRouter( restAPI ) );


    }

    /**
     * 初始化jdbcClient以及authProvider
     */
    private void initJdbcAuth(){

        JsonObject jdbcClientConfig = new JsonObject()
                .put( "provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider" )
                .put( "jdbcUrl", "jdbc:mysql://localhost:3306/dlb?useSSL=true" )
                .put( "username", "root" )
                .put( "password", "root" )
//                .put("driverClassName", "org.postgresql.Driver")
                .put( "maximumPoolSize", 30 );
        jdbcClient = JDBCClient.createShared( vertx, jdbcClientConfig );
        authProvider = new CustomJdbcAuth( vertx, jdbcClient );
    }

    public static void main( String[] args ){
        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions options = new DeploymentOptions();
        options.setInstances( 1 );

        vertx.deployVerticle( AuthWebVerticle.class.getName(), options, res -> {
            if( res.succeeded() ) {
                logger.info( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
            } else {
                res.cause().printStackTrace();
            }
        } );
    }
}
