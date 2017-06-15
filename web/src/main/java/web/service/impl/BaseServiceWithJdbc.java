package web.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import web.service.BaseService;

import java.util.List;

/**
 * Created by liulaoye on 17-6-14.
 * BaseServiceWithJdbc
 */
public abstract class BaseServiceWithJdbc implements BaseService{
    private final JDBCClient jdbcClient;
    protected int page_limit = 10;

    BaseServiceWithJdbc( JDBCClient jdbcClient ){
        this.jdbcClient = jdbcClient;
    }

    protected Future<List<JsonObject>> getByPage(int page, int limit, String sql) {
        JsonArray params = new JsonArray().add(calcPage(page, limit)).add(limit);
        return getConnection().compose(connection -> {
            Future<List<JsonObject>> future = Future.future();
            connection.queryWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    future.complete(r.result().getRows());
                } else {
                    future.fail(r.cause());
                }
                connection.close();
            });
            return future;
        });
    }

    /**
     * Suitable for `add`, `exists` operation.
     *
     * @param params        query params
     * @param sql           sql
     * @param resultHandler async result handler
     */
    protected void executeNoResult( JsonArray params, String sql, Handler<AsyncResult<Void>> resultHandler ){
        jdbcClient.getConnection( connHandler( resultHandler, con -> con.updateWithParams( sql, params, r -> {
            if( r.succeeded() ) {
                resultHandler.handle( Future.succeededFuture() );
            } else {
                resultHandler.handle( Future.failedFuture( r.cause() ) );
            }
            con.close();
        } ) ) );
    }

    /**
     * A helper methods that generates async handler for SQLConnection
     *
     * @return generated handler
     */
    private <R> Handler<AsyncResult<SQLConnection>> connHandler( Handler<AsyncResult<R>> h1, Handler<SQLConnection> h2 ){
        return conn -> {
            if( conn.succeeded() ) {
                final SQLConnection connection = conn.result();
                h2.handle( connection );
            } else {
                h1.handle( Future.failedFuture( conn.cause() ) );
            }
        };
    }
}
