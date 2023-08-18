# Ok 本项目主要针对http请求返回值为json的数据处理，验证，错误记录，重试。
get
添加map或者k，v增加参数
 for (int i = 1; i <=5; i++) {
     System.out.println(Ok.builder().url("https://reqres.in/api/users/"+i).get().sync());
 }

post支持    
json
Form
postUrlEncoded
无法添加Param，可以添加请求头。



多url 自动使用线程池，返回jsonarray
单url 返回jsonobject

调用顺序为 
1.Ok.builder

2.url||param||header||urls

3.get||post

4.sync||async

重试得机制为如果没获取到数据，超时等就重试，如果获取到的数据 code为 200~206则不重试，否则重试。

        <dependency>
            <groupId>cn.glwsq.kukuze</groupId>
            <artifactId>Ok</artifactId>
            <version>0.0.3</version>
        </dependency>
