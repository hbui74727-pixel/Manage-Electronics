package com.mycompany.manage_electronics.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kết nối SQL Server cho Manage Electronics - "chạy được trên mọi máy, không cần sửa code".
 *
 * <p>Cách hoạt động:
 * <ol>
 *   <li><b>Đọc cấu hình</b> theo thứ tự ưu tiên: biến môi trường (DB_USER...) → tham số JVM (-DDB_USER=...)
 *       → file <code>db.properties</code> (classpath, <code>$CATALINA_BASE/conf</code>, thư mục hiện tại)
 *       → giá trị mặc định (sa / 123456 / localhost:1433 / Manage_Electronics).</li>
 *   <li><b>Tự thử mật khẩu dự phòng</b> (mặc định 123456 và 123) nếu mật khẩu cấu hình bị từ chối.</li>
 *   <li><b>Tự thử instance SQLEXPRESS</b> (localhost\SQLEXPRESS) nếu cổng 1433 không phản hồi.</li>
 *   <li><b>Tự tạo database + dữ liệu mẫu</b> bằng file <code>Manage_Electronics.sql</code> nếu đăng nhập được
 *       nhưng database chưa tồn tại (chỉ chạy khi database CHƯA có, không bao giờ xóa dữ liệu đang có).</li>
 *   <li>Ghi nhớ cấu hình đã kết nối thành công nên các lần gọi sau không phải dò lại.</li>
 * </ol>
 * Chữ ký {@link #getConnection()} giữ nguyên nên các DAO không cần sửa.
 */
public class DBConnection {

    private static final Logger LOG = Logger.getLogger(DBConnection.class.getName());

    private static final String SCRIPT_RESOURCE = "/Manage_Electronics.sql";
    private static final int ERR_LOGIN_FAILED = 18456;
    private static final int ERR_CANNOT_OPEN_DB = 4060;

    /** Cấu hình kết nối đã thành công (url + user + password). */
    private static final class Config {
        final String url, user, pass;
        Config(String url, String user, String pass) { this.url = url; this.user = user; this.pass = pass; }
    }

    private static final Object LOCK = new Object();
    private static volatile Config working;
    private static Properties fileProps;
    private static boolean helpPrinted;

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Không tìm thấy SQL Server JDBC Driver (mssql-jdbc). Hãy chạy lại Maven để tải thư viện.", e);
        }
    }

    private DBConnection() { }

    // ------------------------------------------------------------------ API công khai

    public static Connection getConnection() throws SQLException {
        Config c = working;
        if (c != null) {
            try {
                return DriverManager.getConnection(c.url, c.user, c.pass);
            } catch (SQLException e) {
                if (isMissingDatabase(e) && autoInit()) {
                    // Database vừa bị xóa khi ứng dụng đang chạy -> tạo lại
                    initDatabase(c);
                    return DriverManager.getConnection(c.url, c.user, c.pass);
                }
                // Cấu hình cũ không còn đúng (SQL Server đổi cổng/mật khẩu...) -> dò lại từ đầu
                working = null;
            }
        }
        return discover();
    }

    /** Kiểm tra nhanh kết nối (dùng khi khởi động ứng dụng). */
    public static boolean testConnection() {
        try (Connection cn = getConnection()) {
            return cn != null && !cn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ dò cấu hình

    private static Connection discover() throws SQLException {
        synchronized (LOCK) {
            if (working != null) { // luồng khác đã dò xong
                try {
                    return DriverManager.getConnection(working.url, working.user, working.pass);
                } catch (SQLException ignored) {
                    working = null;
                }
            }
            String user = prop("DB_USER", "db.user", "sa");
            List<String> passwords = candidatePasswords();
            SQLException last = null;

            for (String url : candidateUrls()) {
                for (String pass : passwords) {
                    try {
                        Connection cn = DriverManager.getConnection(url, user, pass);
                        working = new Config(url, user, pass);
                        LOG.info("[DBConnection] Kết nối SQL Server thành công: " + url + " (user " + user + ")");
                        return cn;
                    } catch (SQLException e) {
                        last = e;
                        if (isMissingDatabase(e)) {
                            // Đăng nhập ĐÚNG nhưng database chưa có
                            Config cfg = new Config(url, user, pass);
                            if (!autoInit()) {
                                throw withHelp("Database '" + dbName() + "' chưa tồn tại và DB_AUTO_INIT đang tắt. "
                                        + "Hãy chạy file Manage_Electronics.sql bằng SSMS.", e);
                            }
                            initDatabase(cfg);
                            Connection cn = DriverManager.getConnection(url, user, pass);
                            working = cfg;
                            return cn;
                        }
                        if (!isLoginFailed(e)) {
                            break; // server không trả lời ở url này: đổi sang url khác, không thử thêm mật khẩu
                        }
                        // sai mật khẩu: thử mật khẩu kế tiếp
                    }
                }
            }
            throw withHelp("Không thể kết nối SQL Server.", last);
        }
    }

    private static List<String> candidateUrls() {
        List<String> urls = new ArrayList<>();
        String explicit = prop("DB_URL", "db.url", null);
        if (explicit != null) {
            urls.add(explicit);
            return urls;
        }
        String host = prop("DB_HOST", "db.host", "localhost");
        String port = prop("DB_PORT", "db.port", "1433");
        String db = dbName();
        String opts = ";databaseName=" + db + ";encrypt=false;trustServerCertificate=true;loginTimeout=5";

        urls.add("jdbc:sqlserver://" + host + ":" + port + opts);

        if (isLocalHost(host)) {
            String instance = prop("DB_INSTANCE", "db.instance", "SQLEXPRESS");
            if (!instance.isEmpty()) {
                urls.add("jdbc:sqlserver://" + host + ";instanceName=" + instance + opts);
            }
        }
        return urls;
    }

    private static List<String> candidatePasswords() {
        Set<String> set = new LinkedHashSet<>();
        set.add(prop("DB_PASSWORD", "db.password", "123456"));
        String extra = prop("DB_FALLBACK_PASSWORDS", "db.fallbackPasswords", "123456,123");
        for (String p : extra.split(",")) {
            p = p.trim();
            if (!p.isEmpty()) set.add(p);
        }
        return new ArrayList<>(set);
    }

    private static boolean isLocalHost(String host) {
        String h = host.trim().toLowerCase(Locale.ROOT);
        return h.equals("localhost") || h.equals("127.0.0.1") || h.equals("::1") || h.equals(".") || h.equals("(local)");
    }

    private static String dbName() {
        return prop("DB_NAME", "db.name", "Manage_Electronics");
    }

    private static boolean autoInit() {
        String v = prop("DB_AUTO_INIT", "db.autoinit", "true");
        return !(v.equalsIgnoreCase("false") || v.equals("0") || v.equalsIgnoreCase("no"));
    }

    // ------------------------------------------------------------------ nhận diện lỗi

    private static boolean isMissingDatabase(SQLException e) {
        for (SQLException x = e; x != null; x = x.getNextException()) {
            if (x.getErrorCode() == ERR_CANNOT_OPEN_DB) return true;
            String m = x.getMessage();
            if (m != null && m.toLowerCase(Locale.ROOT).contains("cannot open database")) return true;
        }
        return false;
    }

    private static boolean isLoginFailed(SQLException e) {
        for (SQLException x = e; x != null; x = x.getNextException()) {
            if (x.getErrorCode() == ERR_LOGIN_FAILED) return true;
            String m = x.getMessage();
            if (m != null && m.toLowerCase(Locale.ROOT).contains("login failed")) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ tự tạo database

    /** Chạy Manage_Electronics.sql (tách theo dòng GO) nếu database chưa tồn tại. */
    private static void initDatabase(Config cfg) throws SQLException {
        synchronized (LOCK) {
            String masterUrl = toMasterUrl(cfg.url);
            try (Connection cn = DriverManager.getConnection(masterUrl, cfg.user, cfg.pass)) {
                if (databaseExists(cn, dbName())) {
                    return; // đã có (luồng/tiến trình khác vừa tạo) - tuyệt đối không chạy lại script
                }
                LOG.info("[DBConnection] Database '" + dbName() + "' chưa có -> đang tạo từ " + SCRIPT_RESOURCE + " ...");
                List<String> batches = readScriptBatches();
                if (batches.isEmpty()) {
                    throw new SQLException("Không tìm thấy hoặc file rỗng: " + SCRIPT_RESOURCE
                            + " (hãy chắc chắn Manage_Electronics.sql được đóng gói vào classpath).");
                }
                try (Statement st = cn.createStatement()) {
                    for (String b : batches) {
                        st.execute(b);
                    }
                }
                LOG.info("[DBConnection] Đã tạo database và nạp dữ liệu mẫu (" + batches.size() + " khối lệnh).");
            } catch (SQLException e) {
                throw withHelp("Đăng nhập được nhưng không thể tự tạo database '" + dbName() + "'. "
                        + "Hãy chạy thủ công file Manage_Electronics.sql bằng SSMS.", e);
            }
        }
    }

    private static boolean databaseExists(Connection cn, String name) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM sys.databases WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    static String toMasterUrl(String url) {
        if (url.toLowerCase(Locale.ROOT).contains("databasename=")) {
            return url.replaceAll("(?i)databaseName=[^;]*", "databaseName=master");
        }
        return url + (url.endsWith(";") ? "" : ";") + "databaseName=master";
    }

    /** Tách script theo dòng chỉ có "GO" (như SSMS/sqlcmd). */
    static List<String> splitBatches(BufferedReader reader) throws IOException {
        List<String> batches = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) {
                first = false;
                if (!line.isEmpty() && line.charAt(0) == '\uFEFF') line = line.substring(1); // BOM
            }
            if (line.trim().matches("(?i)GO(\\s+\\d+)?\\s*(--.*)?")) {
                addBatch(batches, cur);
                cur.setLength(0);
            } else {
                cur.append(line).append('\n');
            }
        }
        addBatch(batches, cur);
        return batches;
    }

    private static void addBatch(List<String> batches, StringBuilder cur) {
        String s = cur.toString().trim();
        if (!s.isEmpty()) batches.add(s);
    }

    private static List<String> readScriptBatches() throws SQLException {
        try (InputStream in = DBConnection.class.getResourceAsStream(SCRIPT_RESOURCE)) {
            InputStream src = in;
            if (src == null) { // chạy trực tiếp từ thư mục dự án (không qua Maven)
                File f = new File("Manage_Electronics.sql");
                if (!f.isFile()) return new ArrayList<>();
                src = new FileInputStream(f);
            }
            try (BufferedReader r = new BufferedReader(new InputStreamReader(src, StandardCharsets.UTF_8))) {
                return splitBatches(r);
            }
        } catch (IOException e) {
            throw new SQLException("Không đọc được file Manage_Electronics.sql: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ đọc cấu hình

    private static String prop(String envKey, String fileKey, String fallback) {
        String v = System.getenv(envKey);
        if (v != null && !v.trim().isEmpty()) return v.trim();
        v = System.getProperty(envKey);
        if (v != null && !v.trim().isEmpty()) return v.trim();
        Properties p = fileProperties();
        v = p.getProperty(fileKey);
        if (v != null && !v.trim().isEmpty()) return v.trim();
        return fallback;
    }

    private static synchronized Properties fileProperties() {
        if (fileProps != null) return fileProps;
        Properties p = new Properties();
        boolean loaded = false;
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
                loaded = true;
            }
        } catch (Exception ignored) { }
        // File ngoài ghi đè file trong WAR: tiện chỉnh khi đã deploy mà không cần build lại
        List<File> files = new ArrayList<>();
        String base = System.getProperty("catalina.base");
        if (base != null) files.add(new File(base, "conf/db.properties"));
        files.add(new File("db.properties"));
        files.add(new File("src/main/resources/db.properties"));
        for (File f : files) {
            if (f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    Properties ext = new Properties();
                    ext.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
                    p.putAll(ext);
                    loaded = true;
                    LOG.info("[DBConnection] Đã nạp cấu hình từ " + f.getAbsolutePath());
                    break;
                } catch (Exception ignored) { }
            }
        }
        if (!loaded) LOG.fine("[DBConnection] Không có db.properties, dùng giá trị mặc định.");
        fileProps = p;
        return p;
    }

    // ------------------------------------------------------------------ thông báo lỗi

    private static SQLException withHelp(String msg, SQLException cause) {
        String detail = cause == null ? "" : " Chi tiết: " + cause.getMessage();
        if (!helpPrinted) {
            helpPrinted = true;
            System.err.println("================================================================================");
            System.err.println("[Manage Electronics - LỖI CSDL] " + msg + detail);
            System.err.println("Cách khắc phục:");
            System.err.println("  1. Bật dịch vụ SQL Server (RUNNING), bật TCP/IP cổng 1433 và chế độ đăng nhập");
            System.err.println("     'SQL Server and Windows Authentication', bật tài khoản sa.");
            System.err.println("  2. Nếu mật khẩu sa KHÁC 123456 và 123: sửa db.user / db.password trong");
            System.err.println("     src/main/resources/db.properties, hoặc đặt biến môi trường DB_PASSWORD.");
            System.err.println("  3. Không cần tự chạy file SQL: ứng dụng tự tạo database nếu chưa có");
            System.err.println("     (nếu tự động bị lỗi thì mở Manage_Electronics.sql trong SSMS và nhấn F5).");
            System.err.println("================================================================================");
        }
        return new SQLException(msg + detail, cause);
    }

    /** Chạy thử: java -cp ... com.mycompany.manage_electronics.util.DBConnection */
    public static void main(String[] args) {
        System.out.println(testConnection()
                ? "Kết nối cơ sở dữ liệu thành công! (Connection OK)"
                : "Kết nối cơ sở dữ liệu thất bại! (Connection Failed)");
    }
}
