package http;

import http.config.HtmlOkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author yjz
 * @date 2023/5/10$ 11:26$
 * @description:
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args){
        Ok.builder().url("http://114.242.29.130:10161/bh5000/point/node/100014817002/info").get().sync();
        Ok.builder().url("http://114.242.29.130:10161/bh5000/node/info").get().sync();
        Ok.builder().url("http://localhost:8098/").get().sync();
        Ok.builder().url("https://www.bilibili.com/video/BV1ta411g7Jf").get().sync();
        Ok.builder(HtmlOkConfig.getInstance()).url("https://www.bilibili.com/video/BV1ta411g7Jf").get().sync();
        Ok.builder(HtmlOkConfig.getInstance()).url("http://localhost:8098/").get().sync();
        Ok.builder(HtmlOkConfig.getInstance()).url("http://114.242.29.130:10161/bh5000/node/info").get().sync();
    }
    
    
}
