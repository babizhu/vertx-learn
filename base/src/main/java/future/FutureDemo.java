package future;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liukun on 2017/5/11.
 * 用于学习future相关语法
 */
public class FutureDemo{

    private static Vertx vertx = Vertx.vertx();
    private static EventBus eventBus = vertx.eventBus();
    private static Logger logger = LoggerFactory.getLogger( FutureDemo.class );


    private static void init(){

        eventBus.consumer( "address1", res -> logger.info( "consume: " + res.body() ) );
    }

    /**
     * <p>
     * 未使用future时，回调函数嵌在send方法内部，以匿名函数的形式作为send的参数
     * 会引起回调地域
     * </p>
     */
    private static void demo1(){

        //一个复杂一点的例子，产生了回调地狱
        //以下程序先向address1发送一个message，然后等address1回复之后，将address1的回复消息发送给address2......
        eventBus.send( "address1", "value1", asyncResult -> {
            if( asyncResult.succeeded() ) {
                eventBus.send( "address2", asyncResult.result().body(), asyncResult2 -> {
                    if( asyncResult2.succeeded() ) {
                        eventBus.send( "address3", asyncResult2.result().body(), asyncResult3 -> {
                            if( asyncResult3.succeeded() ) {
                                logger.info( asyncResult3.result().body().toString() );
                            } else {
                                asyncResult3.cause().printStackTrace();
                            }
                        } );
                    } else {
                        asyncResult2.cause().printStackTrace();
                    }
                } );
            } else {
                asyncResult.cause().printStackTrace();
            }
        } );
    }

    /**
     * <p>
     * 使用future来避免回调地狱<br>
     * 美观吗？
     * </p>
     */
    private static void demo2(){
        Future<Message<String>> future1 = Future.future();
        Future<Message<String>> future2 = Future.future();
        Future<Message<String>> future3 = Future.future();

        future3.setHandler( asyncResult -> {
            if( asyncResult.succeeded() ) {
                logger.info( asyncResult.result().body() );
            } else {
                asyncResult.cause().printStackTrace();
            }
        } );
        future2.setHandler( asyncResult -> {
            if( asyncResult.succeeded() ) {
                eventBus.send( "address3", asyncResult.result().body(), future3 );
            } else {
                asyncResult.cause().printStackTrace();
            }
        } );
        future1.setHandler( asyncResult -> {
            if( asyncResult.succeeded() ) {
                eventBus.send( "address2", asyncResult.result().body(), future2 );
            } else {
                asyncResult.cause().printStackTrace();
            }
        } );
        eventBus.send( "address1", "value", future1 );

    }

    /**
     * demo1,demo2的写法都太臃肿了，这里是最简洁也最难理解的写法
     */
    private static void composeDemo(){

        Future.<Message<String>>future( f ->
                eventBus.send( "address1", "value", f )
        ).compose( msg ->
                Future.<Message<String>>future( f ->
                        eventBus.send( "address2", msg.body(), f )
                )
        ).compose( msg ->
                Future.<Message<String>>future( f ->
                        eventBus.send( "address3", msg.body(), f )
                )
        ).setHandler( asyncResult -> {
            if( asyncResult.failed() ) {
                asyncResult.cause().printStackTrace();
                return;
            }
            logger.info( asyncResult.result().body() );
        } );

        final Future<Object> future1 = Future.future();
        final Future<Object> future2 = Future.future();
        future2.complete();

        future2.setHandler( future1 );


    }

    private static void httpClientDemo(){
        final HttpClient httpClient = vertx.createHttpClient();
//        httpClient.get( "z.cn", "/", resp -> resp.bodyHandler( body -> System.out.println( body ) ) ).end();
        Future.<HttpClientResponse>future( f ->
                httpClient.get( "z.cn", "/", resp -> f.complete( resp ) ).end()

        ).compose( resp ->
                    Future.<HttpClientResponse>future( f -> {
                            if( resp.statusCode() == 200 ) {
                                System.out.println( "resp.statusCode() == 200" );
                                httpClient.get( "z.cn", "/", Future::succeededFuture ).end();
                            } else {
                                System.out.println( "resp.statusCode() !!!!= 200" );

                                f.fail( "status code is not 200" );
                            }
                        }

                    )).setHandler( resp -> {
                    if( resp.failed() ) {
                        resp.cause().printStackTrace();
                    } else {
                        System.out.println( "success" );
                    }
                } );

    }

    public static void main( String[] args ){
        init();
//        demo1();
//        demo2();
//        composeDemo();
        httpClientDemo();
    }

}