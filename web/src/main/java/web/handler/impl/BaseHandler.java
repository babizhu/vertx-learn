package web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import web.consts.ErrorCode;

/**
 * Created by liulaoye on 17-6-14.
 * 所有Handler的基类
 */
public abstract class BaseHandler{

    /**
     * 根据错误id，构建相应的错误提示信息
     *
     * @param response 　 response
     * @param errId    　    错误ｉｄ
     * @return json
     */
    protected JsonObject buildErrorResponse( HttpServerResponse response, ErrorCode errId ){
        response.setStatusCode( 500 );
        return this.buildResponseJson( errId, "" );
    }

    /**
     * 统一处理错误情况
     *
     * @param errId 错误ｉｄ
     * @param msg   错误的相关参数
     * @return json
     */
    protected JsonObject buildResponseJson( ErrorCode errId, String msg ){
        return new JsonObject().put( "result", errId.toNum() ).put( "msg", msg );
    }

    /**
     * 在没有返回值的情况下，统一返回{"success":true}
     *
     * @return json
     */
    protected JsonObject buildSuccessResponse(){

        return this.buildResponseJson( ErrorCode.SUCCESS, "" );
    }

    protected void sendSuccessResponse( RoutingContext ctx ){
        String res = "{\"result\":true,\"msg\":\"\"}";
        ctx.response().end( res );

    }

    <T> void sendBackResponse( RoutingContext ctx, AsyncResult<T> result ){
        if( result.succeeded() ) {
            sendSuccessResponse( ctx );
        } else {
            sendFailResponse( ctx, (ErrorCode) result.result() );
        }

    }


    protected void sendFailResponse( RoutingContext ctx, ErrorCode errorCode ){
        String res = "{\"result\":" + errorCode.toNum() + ",\"msg\":\"" + errorCode.name() + "\"}";
        ctx.response().setStatusCode( 500 ).end( res );
    }

    abstract protected Router addRouter( io.vertx.ext.web.Router restAPI );

}
