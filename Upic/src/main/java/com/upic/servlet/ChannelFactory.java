package com.upic.servlet;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

//负责创建和管理rmq生命周期，确保每一个通道都是以池化的方式管理
public class ChannelFactory extends BasePooledObjectFactory<Channel> {
  private final Connection connection;

  public ChannelFactory(ConnectionFactory connectionFactory) throws Exception {
    // 建立与RabbitMQ的连接
    this.connection = connectionFactory.newConnection();
  }

  @Override
  public Channel create() throws Exception {
    // 创建新的通道
    return connection.createChannel();
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    // 将通道包装成池化对象
    return new DefaultPooledObject<>(channel);
  }

  @Override
  public void destroyObject(PooledObject<Channel> p) throws Exception {
    // 关闭通道
    p.getObject().close();
  }

  @Override
  public boolean validateObject(PooledObject<Channel> p) {
    // 验证通道是否还开着
    return p.getObject().isOpen();
  }
}
