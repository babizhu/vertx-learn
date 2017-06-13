package web.handler.impl;

import anno.RequirePermissions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by liulaoye on 17-6-12.
 * ProductHandler
 */
public class ProductHandler{
//    private Router restAPI;

//    public ProductHandler( Router mainRouter ){
//        restAPI.route( "/products/add" ).handler( this::add );
//    }

    public Router addRouter( Router restAPI ){
        restAPI.route( "/add" ).handler( this::add );
        restAPI.route( "/del" ).handler( this::del );
        return restAPI;
    }

    @RequirePermissions("sys:product:add")
//    @RequireRoles("admin,sys")
    private void add( RoutingContext ctx ){
        //TODO
        ctx.response().end( "add product" );
    }

    @RequirePermissions("sys:product:del")
//    @RequireRoles("admin,sys")
    private void del( RoutingContext ctx ){
        String productID = ctx.request().getParam( "productid" );

        final String path = ctx.get( "path" );
        ctx.response().end( "path:" + path + "\ndel product " + productID );
    }
}
