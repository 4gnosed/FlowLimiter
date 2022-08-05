## 某电商业务场景题

运营推广部门某次策划上线秒杀或者优惠活动，经测试人员估算压力测试，大约在一个小时内进来100万+用户访问，系统吞吐量固定的情况下，为保障Java服务端正常运行不崩溃，需要对正常访问用户进行限流处理，大约每秒响应1000个请求。   
#### 请问限流的系统如何设计，给出具体的实现？（服务端框架采用spring boot+redis）    
#### 方案
借助redis-cell实现限流，redis-cell是一个由rust语言编写的基于令牌桶算法实现的限流模块（redis4.0扩展）。     
令牌桶算法的原理是定义一个按一定速率产生token的桶，每次去桶中申请token，若桶中没有足够的token则申请失败，否则成功。在请求不多的情况下，桶中的token基本会饱和，此时若流量激增，并不会马上拒绝请求，所以这种算法允许⼀定的流量激增.  
算法关键的两个步骤：  
1.根据上⼀次⽣成令牌时间到现在的时间，及⽣成速率计算出当前令牌桶中的令牌数。     
2.判断令牌桶中是否有⾜够的令牌，并返回结果。     
redis-cell将这两个步骤原子化，通过cl.throttle单个指令来实现流量限制，很方便用于分布式环境中。   
结合场景需求，通过redis-cell每1秒生成1000个令牌，每次请求获取一个令牌，获取成功继续业务流程，获取失败则拒绝请求，达到限流作用保证系统稳定运行。
#### 关键代码
script.setScriptText("return redis.call('cl.throttle',KEYS[1], ARGV[1], ARGV[2], ARGV[3], ARGV[4])[1]");
redisTemplate.execute(script, Arrays.asList(key), maxBurst, tokens, seconds, requestOneTime);
#### 环境准备
redis服务端引入redis-cell模块，配置文件，重启redis-server，验证cl.throttle指令
