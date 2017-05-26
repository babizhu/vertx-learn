package future;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

/**
 * Created by liu_k on 2017/5/20.
 *
 */
public class FutureVercitle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().handler(routingContext -> {

            // 所有的请求都会调用这个处理器处理
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain");

            // 写入响应并结束处理
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response.end("Hello World from Vert.x-Web!\nThe event-loop thread ID is " + Thread.currentThread().getName());
        });

        server.requestHandler(router::accept).listen(8080);
        System.out.println("FutureVercitle.start");
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(10);
        vertx.deployVerticle( FutureVercitle.class.getName(),deploymentOptions );
    }
}
