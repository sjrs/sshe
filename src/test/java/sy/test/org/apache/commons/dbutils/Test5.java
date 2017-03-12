package sy.test.org.apache.commons.dbutils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class Test5 {

	private static Connection conn;

	public static Connection getConnection() {
		String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=172.24.7.189)(PORT=1521))(LOAD_BALANCE=yes)(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=iovdb)))";
		// String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=172.24.176.110)(PORT=1521))(LOAD_BALANCE=yes)(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=orcl)))";
		String driverClassName = "oracle.jdbc.driver.OracleDriver";
		String username = "foton";
		String password = "foton[iov]";
		Connection conn = null;
		DbUtils.loadDriver(driverClassName);
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static void main(String[] args) {
		conn = getConnection();
		QueryRunner qr = new QueryRunner();
		try {
			conn.setAutoCommit(false);
			List<Map<String, Object>> l = qr.query(conn, "select driver.*, councils.name cname from IOV_VL_DEVICE driver, iov_vl_councils councils where councils.id = driver.iov_vl_councils_id order by councils.name", new MapListHandler());
			for (Map<String, Object> driver : l) {
				System.out.println("查询[(" + driver.get("IOV_VL_COUNCILS_ID") + ")" + driver.get("CNAME") + "]的司机[" + driver.get("DRIVER_NAME") + "]");
				List<Map<String, Object>> l2 = qr.query(conn, "select * from IOV_VL_COUNCILS t where name = ?", new MapListHandler(), driver.get("CNAME"));
				if (l2 != null && l2.size() > 0) {
					for (Map<String, Object> councils : l2) {
						BigDecimal ci = (BigDecimal) councils.get("ID");
						BigDecimal oci = (BigDecimal) driver.get("IOV_VL_COUNCILS_ID");
						if (ci != oci) {
							List<Map<String, Object>> l3 = qr.query(conn, "select driver.* from IOV_VL_DEVICE driver where driver.iov_vl_councils_id = ? and driver.driver_name=? and driver.driver_phone=?", new MapListHandler(), councils.get("ID"), driver.get("DRIVER_NAME"), driver.get("DRIVER_PHONE"));
							if (l3 == null || l3.size() < 1) {
								System.out.println("[(" + ci + ")" + councils.get("NAME") + "]承运商没有[" + driver.get("DRIVER_NAME") + "]司机信息");
								qr.update(conn, "insert into iov_vl_device (id, iov_biz_device_id, driver_name, driver_phone, created, modified, cbm_mag_company_id, iov_vl_councils_id, driver_idcard) values (SEQ_IOV_VL_DEVICE.Nextval, ?, ?, ?, sysdate, sysdate, 1020, ?, ?)", driver.get("IOV_BIZ_DEVICE_ID"), driver.get("DRIVER_NAME"), driver.get("DRIVER_PHONE"), councils.get("ID"), driver.get("DRIVER_IDCARD"));
								conn.commit();
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			try {
				DbUtils.rollback(conn);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				DbUtils.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
