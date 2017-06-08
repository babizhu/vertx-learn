package auth;

import auth.util.DefaultHashStrategy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by liulaoye on 17-6-6.
 * 自定义的web Auth 类
 */
public class CustomJdbcAuth implements AuthProvider{

    private static final String DEFAULT_AUTHENTICATE_QUERY = "SELECT PASSWORD, PASSWORD_SALT FROM USER WHERE USERNAME = ?";

    /**
     * The default query to retrieve all roles for the user
     */
    private static final String DEFAULT_ROLES_QUERY = "SELECT ROLE FROM USER_ROLES WHERE USERNAME = ?";

    /**
     * The default query to retrieve all permissions for the role
     */
    private static final String DEFAULT_PERMISSIONS_QUERY = "SELECT PERM FROM ROLES_PERMS WHERE ROLE IN(?)";

    /**
     * The default role prefix
     */
    public static final String DEFAULT_ROLE_PREFIX = "role:";

    private final JDBCClient client;
    private final DefaultHashStrategy hashStrategy;

    public CustomJdbcAuth( Vertx vertx ,JDBCClient client ){
        this.client = client;
        this.hashStrategy = new DefaultHashStrategy( vertx );
    }

    @Override
    public void authenticate( JsonObject authInfo, Handler<AsyncResult<User>> resultHandler ){

        String userName = authInfo.getString( "username" );
        if( userName == null ) {
            resultHandler.handle( Future.failedFuture( "authInfo must contain userName in 'userName' field" ) );
            return;
        }
        String password = authInfo.getString( "password" );
        if( password == null ) {
            resultHandler.handle( Future.failedFuture( "authInfo must contain password in 'password' field" ) );
            return;
        }

        executeQuery( DEFAULT_AUTHENTICATE_QUERY, new JsonArray().add( userName ), resultHandler, rs -> {
            switch( rs.getNumRows() ) {

                case 0: {
                    // Unknown user/password
                    resultHandler.handle( Future.failedFuture( "Invalid userName/password" ) );
                    break;
                }
                case 1: {
                    JsonArray row = rs.getResults().get( 0 );
                    String hashedStoredPwd = hashStrategy.getHashedStoredPwd( row );
                    String salt = hashStrategy.getSalt( row );
                    String hashedPassword = hashStrategy.computeHash( password, salt );
                    if( hashedStoredPwd.equals( hashedPassword ) ) {
                        executeQuery( DEFAULT_ROLES_QUERY, new JsonArray().add( userName ), resultHandler, rs1 -> {
                            final Set<String> roles = rs1.getRows().stream().map( entries -> entries.getString( "ROLE" ) ).collect( Collectors.toSet() );
                            final String  permissionCondition = roles.stream().map(item->"'"+item+"'").collect( Collectors.joining(",") );
                            final String permissionSql = DEFAULT_PERMISSIONS_QUERY.replace( "?", permissionCondition );

                            executeQuery( permissionSql, null, resultHandler, rs2 -> {//
                                final Set<String> permissions = rs2.getRows().stream().map( entries -> entries.getString( "PERM" ) ).collect( Collectors.toSet() );
                                resultHandler.handle( Future.succeededFuture( new CustomWebUser( userName,roles,permissions,this ) ) );
                            } );

                        } );

                    } else {
                        resultHandler.handle( Future.failedFuture( "Invalid userName/password" ) );
                    }
                    break;
                }
                default: {
                    // More than one row returned!
                    resultHandler.handle( Future.failedFuture( "Failure in authentication" ) );
                    break;
                }
            }
        } );
    }


    private <T> void executeQuery( String query, JsonArray params, Handler<AsyncResult<T>> resultHandler,
                                   Consumer<ResultSet> resultSetConsumer ){
        client.getConnection( res -> {
            if( res.succeeded() ) {
                SQLConnection conn = res.result();
                conn.queryWithParams( query, params, queryRes -> {
                    if( queryRes.succeeded() ) {
                        ResultSet rs = queryRes.result();
                        resultSetConsumer.accept( rs );
                    } else {
                        resultHandler.handle( Future.failedFuture( queryRes.cause() ) );
                    }
                    conn.close( closeRes -> {
                    } );
                } );
            } else {
                resultHandler.handle( Future.failedFuture( res.cause() ) );
            }
        } );
    }
}
