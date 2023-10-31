package http;

/**
 * @author yjz
 * @date 2023/5/10$ 11:26$
 * @description:
 */
public class Main {
    public static void main(String[] args) {
        System.out.println(Ok.builder().url("http://114.242.29.130:10161/bh5000/node/info").get().sync());
    }
}
