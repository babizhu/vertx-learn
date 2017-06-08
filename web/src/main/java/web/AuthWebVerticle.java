package web;

import auth.CustomJdbcAuth;
import auth.CustomWebUser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

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

        router.route( "/private/*" ).handler( basicAuthHandler );
        router.route( "/private/*" ).handler( ctx->{
            final CustomWebUser user = (CustomWebUser) ctx.user();

            ctx.response().end(user.getRoles().toString());
        } );

        server.requestHandler( router::accept ).listen( PORT );
    }

    /**
     * 初始化jdbcClient以及authProvider
     */
    private void init(){
        System.out.println(config().getString("/private/product/add"));
        JsonObject jdbcClientConfig = new JsonObject()
                .put( "provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider" )
                .put( "jdbcUrl", "jdbc:mysql://localhost:3306/dlb?useSSL=true" )
                .put( "username", "root" )
                .put( "password", "root" )
//                .put("driverClassName", "org.postgresql.Driver")
                .put( "maximumPoolSize", 30 );
        JDBCClient jdbcClient = JDBCClient.createShared( vertx, jdbcClientConfig );
        authProvider = new CustomJdbcAuth( vertx, jdbcClient );
        System.out.println("AuthWebVerticle.init");
//        this.config()
    }


    public static void main( String[] args ){
        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 100000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions options = new DeploymentOptions();
        options.setInstances( 2 );

        options.setConfig(new JsonObject().put("/private/product/add","admin"));
        vertx.deployVerticle( AuthWebVerticle.class.getName(), options, res -> {
            if( res.succeeded() ) {
                System.out.println( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
            }
        } );

    }
}
