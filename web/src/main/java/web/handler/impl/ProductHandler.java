package web.handler.impl;

import anno.RequirePermissions;
import io.vertx.core.json.Json;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import web.consts.ErrorCode;
import web.pojo.Product;
import web.service.impl.ProductService;

/**
 * Created by liulaoye on 17-6-12.
 * ProductHandler
 */
public class ProductHandler extends BaseHandler{

    private final ProductService service;

    public ProductHandler( JDBCClient jdbcClient ){
        service = new ProductService( jdbcClient );
    }
//    private Router restAPI;

//    public ProductHandler( Router mainRouter ){
//        restAPI.route( "/products/add" ).handler( this::add );
//    }

    @Override
    public Router addRouter( Router restAPI ){
        restAPI.route( "/add" ).handler( this::add );
        restAPI.route( "/del" ).handler( this::del );

        return restAPI;
    }

    @RequirePermissions("sys:product:add")
//    @RequireRoles("sys")
    private void add( RoutingContext ctx ){
        Product product = new Product( 22, "name" );

        service.add( product, ( a ) -> ctx.response().end( Json.encode( product ) ) );
    }

    @RequirePermissions("sys:product:del")
//    @RequireRoles("admin,sys")
    private void del( RoutingContext ctx ){
        String productID = ctx.request().getParam( "productid" );
        if( productID == null || productID.isEmpty() ) {
            sendFailResponse( ctx, ErrorCode.SYS_PARAMETER_ERROR );
            return;
        }
        service.del( productID, ( result ) -> sendBackResponse( ctx, result ) );
    }
}
