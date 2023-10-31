package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yjz
 * @date 2023/5/10$ 11:26$
 * @description:
 */
public class Main {
    protected final static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        String sync = Ok.builder().url("http://114.242.29.130:10161/bh5000/node/info").get().sync();




    }

}

