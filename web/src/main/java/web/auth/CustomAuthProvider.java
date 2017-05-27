package web.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * Created by liu_k on 2017/5/27.
 * 自己提供的authProvider
 */
public class CustomAuthProvider implements AuthProvider{
    @Override
    public void authenticate( JsonObject authInfo, Handler<AsyncResult<User>> resultHandler ){
        String username = authInfo.getString( "username" );
        String password = authInfo.getString( "password" );
        if( username.equals( "aa" ) && password.equals( "bb" )){
            resultHandler.handle( Future.succeededFuture(new CustomUser(username, this, "rolePrefix")));
        }else {
            resultHandler.handle( Future.failedFuture( "user or password invalid!" ) );
        }
    }
}
