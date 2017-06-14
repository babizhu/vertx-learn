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
     * @param a
     */
    public ProductService add(Product a, Handler<AsyncResult<Void>> resultHandler ){
        JsonArray params = new JsonArray().add("a");
        executeNoResult( params,"insert ", resultHandler);
        return this;

    }
}
