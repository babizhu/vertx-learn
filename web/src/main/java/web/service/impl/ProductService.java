package web.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import web.pojo.Product;

/**
 * Created by liulaoye on 17-6-14.
 * 产品模块逻辑
 */
public class ProductService extends BaseServiceWithJdbc{
    public ProductService( JDBCClient jdbcClient ){
        super( jdbcClient );
    }

    /**
     * 增加一个商品
     *
     * @param a
     */
    public ProductService add( Product product, Handler<AsyncResult<Void>> resultHandler ){
        JsonArray params = new JsonArray().add( product.getId() )
                .add( product.getName() );
        this.executeNoResult( params, INSERT_STATEMENT, resultHandler );
        return this;

    }

    public ProductService del( String  id, Handler<AsyncResult<Void>> resultHandler ){
        JsonArray params = new JsonArray().add( id );
        this.executeNoResult( params, DELETE_STATEMENT, resultHandler );
        return this;

    }

    public ProductService getProductsByPage( int page, Handler<AsyncResult<Void>> resultHandler ){
        JsonArray params = new JsonArray().add( id );
        this.executeNoResult( params, DELETE_STATEMENT, resultHandler );
        return this;

    }

    private static final String INSERT_STATEMENT = "insert int product (id, name) values (?, ?)";
    private static final String DELETE_STATEMENT = "delete product where id = ?";

}
