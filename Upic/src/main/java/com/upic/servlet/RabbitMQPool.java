package com.upic.servlet;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RabbitMQPool {
  //管理通道的池，利用自定义的channel factory来维护通道
  private GenericObjectPool<Channel> pool;

  public RabbitMQPool() throws Exception {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("44.228.45.79"); // RabbitMQ服务器地址
    connectionFactory.setUsername("yuhan"); // 用户名
    connectionFactory.setPassword("yuhan"); // 密码

    // 创建并配置通道池
    this.pool = new GenericObjectPool<>(new ChannelFactory(connectionFactory));

    // 配置池参数（可选）
    GenericObjectPoolConfig<Channel> config = new GenericObjectPoolConfig<>();
    config.setMaxTotal(10); // 池中最大通道数
    config.setMaxIdle(5); // 池中最大空闲通道数
    config.setMinIdle(2); // 池中最小空闲通道数
    this.pool.setConfig(config);
  }
//从池借用和归还
  public Channel borrowChannel() throws Exception {
    return pool.borrowObject();
  }

  public void returnChannel(Channel channel) {
    try {
      pool.returnObject(channel);
    } catch (Exception e) {
      // 处理归还通道时的异常
    }
  }

  // 关闭池，释放资源
  public void close() {
    if (pool != null && !pool.isClosed()) {
      pool.close();
    }
  }
}

