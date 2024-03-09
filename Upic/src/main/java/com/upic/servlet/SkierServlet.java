package com.upic.servlet;

import org.json.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

//Assign1 part servlet
@WebServlet("/skiers")
public class SkierServlet extends HttpServlet {
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

      response.setStatus(HttpServletResponse.SC_CREATED);
      response.getWriter().write("Skier data saved successfully!");
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid JSON format");
    }
  }
}
