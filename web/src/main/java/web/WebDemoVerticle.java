package web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

import java.time.LocalTime;

/**
 * Created by liu_k on 2017/5/24.
 * vertx web 演示
 */
public class WebDemoVerticle extends AbstractVerticle {
    private static final int PORT = 8000;

    @Override
    public void start() throws Exception {
//        super.start();
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        setSubRoute( router );
        setBlockRoute(router);
        setReadUrlParmRoute( router );
        setTimerRoute(router);
        setReroute(router);
        set404Route(router);

//        router.route().handler(ctx -> {
//
//            // 所有的请求都会调用这个处理器处理
//            HttpServerResponse response = ctx.response();
//            response.putHeader("content-type", "text/plain");
//
//            // 写入响应并结束处理
//            response.end(Thread.currentThread().getName() + "\n" + "Hello World from Vert.x-Web!("+LocalTime.now()+")");
//        });

        server.requestHandler(router::accept).listen(PORT);
        System.out.println("web server started at port " + PORT + ", please click http://localhost:8000 to visit!");
    }

    private void setSubRoute( Router mainRouter ){
        Router restAPI = Router.router(vertx);

        restAPI.get("/products/search/:productID").handler(rc -> {

            // TODO 查找产品信息
            rc.response().end("查找产品信息");

        });

        restAPI.get("/products/add/:productID").handler(rc -> {

            // TODO 添加新的产品 http://localhost:8000/productsAPI/products/add/100
            rc.response().end("添加新的产品");

        });

        restAPI.get("/products/del/:productID").handler(rc -> {

            // TODO 删除产品
            rc.response().end("删除产品");

        });

        mainRouter.mountSubRouter("/productsAPI", restAPI);
    }

    /**
     * 路由转发
     * @param router    router
     */
    private void setReroute(Router router){
        router.get("/some/path").handler(ctx -> {

            ctx.put("foo", "bar");
            ctx.next();

        });

        router.get("/some/path/B").handler(ctx -> ctx.response().end("reRoute param foo'value is " + ctx.get("foo")) );

        router.get("/some/path").handler(ctx -> ctx.reroute("/some/path/B") );
    }
    /**
     * 在另外的工作线程执行某些阻塞的耗时操作
     * @param router    router
     */
    private void setBlockRoute(Router router){
        final String path = "/block";

        router.route(path).blockingHandler(ctx -> {
            HttpServerResponse response = ctx.response();

            LocalTime  now = LocalTime.now();
//            response.setChunked(true);
            response.write( Thread.currentThread().getName() + "("+now+")\n" );

            // 执行某些同步的耗时操作
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            response.write( Thread.currentThread().getName() );
            // 调用下一个处理器
            response.end(Thread.currentThread().getName() + "("+LocalTime.now()+")\n");

        });
    }

    /**
     * 延时处理数据
     * 顺便演示setChunked()的用法
     * @param router    router
     */
    private void setTimerRoute(Router router){
        final String path = "/timer";
        router.route(path).handler(ctx -> {

            HttpServerResponse response = ctx.response();
            // 由于我们会在不同的处理器里写入响应，因此需要启用分块传输
            // 仅当需要通过多个处理器输出响应时才需要
            response.setChunked(true);

            response.write("route1\n");

            // 5 秒后调用下一个处理器
            ctx.vertx().setTimer(5000, tid -> ctx.next());
        });

        router.route(path).handler(ctx -> {

            HttpServerResponse response = ctx.response();
            response.write("route2\n");

            // 5 秒后调用下一个处理器
            ctx.vertx().setTimer(5000, tid ->  ctx.next());
        });

        router.route(path).handler(ctx -> {

            HttpServerResponse response = ctx.response();
            response.write("route3");

            // 结束响应
            ctx.response().end();
        });
    }

    /**
     * 采用reRoute来处理404错误，需要注意的是reRoute会修改当前的状态码和失败原因，因此需要自己再次设置
     * @param router    router
     */
    private void set404Route(Router router){
        router.get("/my-pretty-notfound-handler").handler(ctx -> ctx.response()
                .setStatusCode(404)
                .end("NOT FOUND fancy html here!!!") );

        router.route().failureHandler(ctx -> {
            if (ctx.statusCode() == 404) {
                ctx.reroute("/my-pretty-notfound-handler");
            } else {
                ctx.next();
            }
        });
        router.route().handler(ctx -> ctx.fail( 404) );
    }



    /**
     * 获取url上的参数，
     *
     * @param router    router
     */
    private void setReadUrlParmRoute( Router router){
        final String path = "/products/:producttype/:productid";

        router.route( path ).handler(ctx -> {

            String productType = ctx.request().getParam("producttype");
            String productID = ctx.request().getParam("productid");
            ctx.response().end("producttype = " + productType + ", productid = " + productID);

            // 执行某些操作...
        });
    }
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle( WebDemoVerticle.class.getName());
    }
}
