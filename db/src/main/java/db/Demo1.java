package db;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

/**
 * Created by liulaoye on 17-5-12.
 * db测试
 */
public class Demo1 extends AbstractVerticle{
    private JDBCClient client;

    public void start(){
        JsonObject config = new JsonObject()
                .put( "url", "jdbc:mysql://localhost:3306/dlb?useSSL=true" )
                .put( "user", "root" )
                .put( "password", "root" )
//                .put( "driverClassName", "com.zaxxer.hikari" )
                .put( "maximumPoolSize", 30 );

        client = JDBCClient.createShared( vertx, config );
        client.getConnection( res -> {
            if( res.succeeded() ) {

                SQLConnection connection = res.result();

                connection.query( "SELECT * FROM Rainfall", res2 -> {
                    if( res2.succeeded() ) {

                        ResultSet rs = res2.result();

                        for( JsonObject entries : rs.getRows() ) {
                            System.out.println( entries );
                        }
                        // 用结果集results进行其他操作
                    }
                } );
            } else {
                // 获取连接失败 - 处理失败的情况
            }
        } );
    }

    public static void main( String[] args ){
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle( Demo1.class.getName() );
    }
}
