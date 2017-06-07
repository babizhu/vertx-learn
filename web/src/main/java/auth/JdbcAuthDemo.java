package auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * Created by liulaoye on 17-5-26.
 * jdbcAuth演示代码
 */
public class JdbcAuthDemo extends AbstractVerticle{
    @Override
    public void start() throws Exception{

        super.start();
        JsonObject jdbcClientConfig = new JsonObject()
                .put( "provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider" )
                .put( "jdbcUrl", "jdbc:mysql://localhost:3306/dlb?useSSL=true" )
                .put( "username", "root" )
                .put( "password", "root" )
//                .put("driverClassName", "org.postgresql.Driver")
                .put( "maximumPoolSize", 30 );
        JDBCClient jdbcClient = JDBCClient.createShared( vertx, jdbcClientConfig );

//        JDBCAuth authProvider = JDBCAuth.create( vertx, jdbcClient );
        CustomJdbcAuth authProvider = new CustomJdbcAuth( vertx, jdbcClient );
//        authProvider.setRolesQuery("SELECT PERM FROM roles_perms RP, user_roles UR WHERE UR.USERNAME = ? AND UR.ROLE = RP.ROLE"  );//解决表大小写的问题，真麻烦
//insertUser( authProvider,jdbcClient );
//Thread.sleep( 1000 );
        JsonObject authInfo = new JsonObject().put("username", "tim").put("password", "sausages");

        authProvider.authenticate(authInfo, res -> {
            if (res.succeeded()) {
                User user = res.result();
                System.out.println("验证成功！！！");
                final JsonObject principal = user.principal();
                user.isAuthorised( "product:add", au -> {
                    if(au.succeeded()){
                        if( au.result() )
                        System.out.println("role check success");
                        else{
                            System.out.println("role check faild");

                        }
                    }else {
                        au.cause().printStackTrace();
                    }
                } );
                user.isAuthorised( "role:sys",au-> {
                    if(au.succeeded()){
                        if(au.result() )
                        System.out.println("用户拥有角色sys");
                        else{
                            System.out.println("用户没有拥有角色sys");
                        }
                    }else {
                        au.cause().printStackTrace();
                    }
                } );
                System.out.println(principal);

            } else {
                res.cause().printStackTrace();
            }
        });

    }

    private void insertUser(JDBCAuth authProvider,JDBCClient jdbcClient){
        String salt = authProvider.generateSalt();
        String hash = authProvider.computeHash("sausages", salt);
// save to the database
        jdbcClient.getConnection( res -> {
            if( res.succeeded() ) {

                SQLConnection con = res.result();
                con.updateWithParams("INSERT INTO USER VALUES (?, ?, ?)", new JsonArray().add("tim").add(hash).add(salt), res1 -> {
                    if (res1.succeeded()) {
                        System.out.println( "add user success!!!" );
                    }else{
                        res1.cause().printStackTrace();
                    }
                });
                }


        } );

    }
    public static void main( String[] args ){
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle( JdbcAuthDemo.class.getName() );
    }
}
