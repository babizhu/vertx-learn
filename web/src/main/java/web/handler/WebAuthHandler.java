package web.handler;

import io.vertx.core.Handler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import web.handler.impl.WebAuthHandlerImpl;

/**
 * Created by liulaoye on 17-6-12.
 * interface
 */
public interface WebAuthHandler extends Handler<RoutingContext>{
    static WebAuthHandler create( AuthProvider authProvider) {
        return new WebAuthHandlerImpl(authProvider);
    }

}
