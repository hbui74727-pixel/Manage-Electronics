package com.mycompany.manage_electronics.listener;

import com.mycompany.manage_electronics.util.DBConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;

/**
 * Chạy khi Tomcat nạp ứng dụng: dò kết nối SQL Server và tự tạo database nếu chưa có,
 * để lỗi (nếu có) hiện ngay trong log thay vì chờ tới request đầu tiên.
 */
@WebListener
public class DatabaseInitListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(DatabaseInitListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        boolean ok = DBConnection.testConnection();
        LOG.info(ok ? "[Manage Electronics] CSDL sẵn sàng."
                    : "[Manage Electronics] CHƯA kết nối được CSDL - xem hướng dẫn ở log phía trên.");
    }
}
