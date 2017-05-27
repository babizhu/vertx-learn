package web.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

/**
 * Created by liu_k on 2017/5/27.
 * 自定义的用户类
 */
public class CustomUser extends AbstractUser{

    private final String username;
    private final AuthProvider authProvider;
    private final String rolePrefix;

    public CustomUser( String username, AuthProvider customAuthProvider, String rolePrefix ){
        this.username = username;
        this.authProvider = customAuthProvider;
        this.rolePrefix = rolePrefix;
    }

    @Override
    protected void doIsPermitted( String permission, Handler<AsyncResult<Boolean>> resultHandler ){

    }

    @Override
    public JsonObject principal(){
        return null;
    }

    @Override
    public void setAuthProvider( AuthProvider authProvider ){

    }
}
