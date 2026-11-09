package com.mycompany.manage_electronics.dao;

import java.text.Normalizer;

/**
 * Hỗ trợ tìm kiếm không phân biệt hoa/thường và không phân biệt dấu tiếng Việt
 * ("dien thoai" tìm được "Điện thoại"). Dùng chung cho các DAO.
 */
final class ProductSearchHelper {
    private ProductSearchHelper() {}

    /** Biểu thức SQL: bỏ dấu + chữ đ rồi so sánh theo collation không phân biệt dấu. */
    static String col(String column) {
        return "REPLACE(" + column + ", NCHAR(273), N'd') COLLATE Latin1_General_100_CI_AI";
    }

    /** Chuẩn hóa từ khóa: đ -> d, rồi bỏ dấu để khớp với phía SQL. */
    static String stripD(String s) {
        String t = s.replace('đ', 'd').replace('Đ', 'D');
        // Giữ nguyên dấu: SQL (collation AI) tự bỏ dấu; chỉ cần chuẩn hóa dạng Unicode.
        return Normalizer.normalize(t, Normalizer.Form.NFC);
    }

    /** Thoát các ký tự đặc biệt của LIKE (dùng cùng ESCAPE '\'). */
    static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_").replace("[", "\\[");
    }
}
