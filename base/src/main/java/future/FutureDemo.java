package future;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liukun on 2017/5/11.
 * 用于学习future相关语法
 */
public class FutureDemo {

    private static Vertx vertx = Vertx.factory.vertx();
    private static EventBus eventBus = vertx.eventBus();
    private static Logger logger = LoggerFactory.getLogger(FutureDemo.class);


    private static void init(){
        eventBus.consumer("address1", res-> System.out.println(res.body()));
    }
    /**
     * 未使用future时，回调函数嵌在send方法内部，以匿名函数的形式作为send的参数
     */
    private static void demo1() {

        //一个复杂一点的例子，产生了回调地狱
        //以下程序先向address1发送一个message，然后等address1回复之后，将address1的回复消息发送给address2......
        eventBus.send("address1", "value1", asyncResult -> {
            if (asyncResult.succeeded()) {
                eventBus.send("address2", asyncResult.result().body(), asyncResult2 -> {
                    if (asyncResult2.succeeded()) {
                        eventBus.send("address3", asyncResult2.result().body(), asyncResult3 -> {
                            if (asyncResult3.succeeded()) {
                                logger.info(asyncResult3.result().body().toString());
                            } else {
                                asyncResult3.cause().printStackTrace();
                            }
                        });
                    } else {
                        asyncResult2.cause().printStackTrace();
                    }
                });
            } else {
                asyncResult.cause().printStackTrace();
            }
        });
    }

    /**
     * 使用future来避免回调地狱
     */
    public static void demo2() {
        Future<Message<String>> future = Future.future();
        future.setHandler(asyncResult -> System.out.println(asyncResult.result().body()));
        eventBus.send("address1", "value", future);

    }

    public static void main(String[] args) {
        init();
        demo1();
//        demo2();
    }
}
