package com.upic.servlet;
import org.json.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import org.apache.commons.pool2.impl.GenericObjectPool;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

@WebServlet("/liftRides")
public class LiftRideServlet extends HttpServlet {
  //通道池，用于管理通道的池，利用channelfactory来创建和维护
  private GenericObjectPool<Channel> channelPool;

  @Override
  public void init() throws ServletException {
    super.init();
    try {
      ConnectionFactory connectionFactory = new ConnectionFactory();
      connectionFactory.setHost("44.228.45.79"); // 修改为您的RabbitMQ服务器地址
      connectionFactory.setUsername("yuhan"); // 修改为您的用户名
      connectionFactory.setPassword("yuhan"); // 修改为您的密码
      // 初始化通道池
      this.channelPool = new GenericObjectPool<>(new ChannelFactory(connectionFactory));
    } catch (Exception e) {
      throw new ServletException("Failed to create channel", e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    try (BufferedReader reader = request.getReader()) {
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Error reading request body");
      return;
    }

    String requestBody = sb.toString();

    try {
      JSONObject jsonObject = new JSONObject(requestBody);
      int skierId = jsonObject.optInt("skierId", -1);
      int resortId = jsonObject.optInt("resortId", -1);
      int liftId = jsonObject.optInt("liftId", -1);
      int seasonId = jsonObject.optInt("seasonId", -1);
      int dayId = jsonObject.optInt("dayId", -1);
      int time = jsonObject.optInt("time", -1);

      if (skierId < 1 || skierId > 100000 || resortId < 1 || resortId > 10 ||
          liftId < 1 || liftId > 40 || seasonId != 2024 || dayId != 1 ||
          time < 1 || time > 360) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("Invalid parameters provided");
        return;
      }

      // 数据验证通过，使用通道池发布到RabbitMQ
      Channel channel = null;
      try {
        channel = channelPool.borrowObject();
        String queueName = "liftRidesQueue"; // 定义您的队列名称
        channel.queueDeclare(queueName, true, false, false, null);
        String message = jsonObject.toString();
        channel.basicPublish("", queueName, null, message.getBytes());
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write("Skier data saved successfully!");
      } finally {
        if (channel != null) {
          channelPool.returnObject(channel);
        }
      }
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid JSON format or channel pool error");
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    channelPool.close(); // 关闭通道池，释放资源
  }
}
