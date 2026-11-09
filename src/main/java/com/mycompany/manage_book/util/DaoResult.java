package com.mycompany.manage_book.util;

import java.sql.SQLException;

/** Mã kết quả cho các thao tác xóa/đặt hàng để servlet hiển thị thông báo đúng. */
public final class DaoResult {
    public static final int OK = 1;
    public static final int FAIL = 0;
    public static final int IN_USE = -1;     // bị ràng buộc khóa ngoại (đang được tham chiếu)
    public static final int OUT_OF_STOCK = -2;
    public static final int BAD_VOUCHER = -3;

    private DaoResult() {}

    /** SQL Server báo lỗi vi phạm khóa ngoại/ràng buộc với SQLState 23000. */
    public static boolean isConstraintViolation(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }
}
