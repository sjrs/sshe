package sy.test.org.apache.commons.dbutils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class CountHisPhone {

	private static Connection conn;

	public static Connection getConnection() {
		String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=172.24.7.189)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=iovdb)))";
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
			conn.setAutoCommit(true);
			String startDate = "2014-1-1";
			String endDate = "2014-2-1";
			String sql = "";
			sql += "SELECT HIS_PHONE.PHONE, COUNT(1)                                      ";
			sql += "  FROM HIS_PHONE                                                      ";
			sql += " WHERE HIS_PHONE.STATUS = 1                                           ";
			sql += "   AND HIS_PHONE.CBM_MAG_COMPANY_ID = 1020                            ";
			sql += "   AND HIS_PHONE.CREATED BETWEEN DATE '" + startDate + "' AND DATE '" + endDate + "' ";
			sql += " GROUP BY HIS_PHONE.PHONE                                             ";
			List<Map> l = (List) qr.query(conn, sql, new MapListHandler());
			System.out.println(l);
			if (l != null && l.size() > 0) {
				Map councilsMap = new HashMap();
				for (Map m : l) {
					sql = "";
					sql += "SELECT IOV_VL_DEVICE.DRIVER_PHONE,                                    ";
					sql += "       IOV_VL_DEVICE.IOV_VL_COUNCILS_ID,                              ";
					sql += "       IOV_VL_COUNCILS.CODE,                                          ";
					sql += "       IOV_VL_COUNCILS.NAME,                                          ";
					sql += "       CBM_MAG_ORGANIZATION.CODE,                                     ";
					sql += "       CBM_MAG_ORGANIZATION.NAME                                      ";
					sql += "  FROM IOV_VL_DEVICE                                                  ";
					sql += "  LEFT JOIN IOV_VL_COUNCILS                                           ";
					sql += "    ON IOV_VL_DEVICE.IOV_VL_COUNCILS_ID = IOV_VL_COUNCILS.ID          ";
					sql += "  LEFT JOIN IOV_VL_L_COUNCILS_ORG                                     ";
					sql += "    ON IOV_VL_COUNCILS.ID = IOV_VL_L_COUNCILS_ORG.IOV_VL_COUNCILS_ID  ";
					sql += "  LEFT JOIN CBM_MAG_ORGANIZATION                                      ";
					sql += "    ON IOV_VL_L_COUNCILS_ORG.CBM_MAG_ORGANIZATION_ID =                ";
					sql += "       CBM_MAG_ORGANIZATION.ID                                        ";
					sql += " WHERE IOV_VL_DEVICE.DRIVER_PHONE = '" + m.get("PHONE") + "'                     ";
					List<Map> l2 = (List) qr.query(conn, sql, new MapListHandler());
					if (l2 != null && l2.size() > 0) {
						System.out.println(l2.get(0));
						// councilsMap.put("", l2.get(0));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				DbUtils.commitAndClose(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
