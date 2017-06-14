package web.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by liukun on 2017/6/14.
 * pojo product
 */
@Data
@Slf4j
@AllArgsConstructor
public  class Product {
    private int id;
    private String name;

    public static void main(String[] args) {
        log.error("Calling the 'CounterLog' with a message");

    }

}
