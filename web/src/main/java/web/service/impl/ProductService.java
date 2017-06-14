package web.service.impl;

import io.vertx.ext.jdbc.JDBCClient;

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
    public void add( String a ){

    }
}
