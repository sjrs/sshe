package sy.test.org.apache.commons.dbutils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class Test6 {

	private static Connection conn;

	public static Connection getConnection() throws SQLException {
		String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=172.24.7.189)(PORT=1521))(LOAD_BALANCE=yes)(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=iovdb)))";
		// String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=172.24.176.110)(PORT=1521))(LOAD_BALANCE=yes)(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=orcl)))";
		String driverClassName = "oracle.jdbc.driver.OracleDriver";
		String username = "foton";
		String password = "foton[iov]";
		Connection conn = null;
		DbUtils.loadDriver(driverClassName);
		conn = DriverManager.getConnection(url, username, password);
		return conn;
	}

	public static void main(String[] args) throws SQLException {
		conn = getConnection();
		QueryRunner q = new QueryRunner();
		String sql = "select * from IOV_VL_ORDER t WHERE t.status = 3 and created>=to_date('2014-06-01','yyyy-MM-dd') ORDER BY t.begin_date DESC";
		List<Map<String, Object>> orderList = q.query(conn, sql, new MapListHandler());
		if (orderList != null && orderList.size() > 0) {
			for (Map<String, Object> order : orderList) {
				System.out.println("orderNum[" + order.get("ORDER_NUM") + "]vin[" + order.get("VIN") + "]");
				if (order.get("IOV_VL_PLACE_POINT_END_ID") != null && order.get("FIXED_TYPE") != null && order.get("IOV_VL_DEVICE_ID") != null) {
					sql = "select * from IOV_VL_PLACE_POINT t WHERE ID = " + order.get("IOV_VL_PLACE_POINT_END_ID") + "";
					Map<String, Object> endPlace = q.query(conn, sql, new MapHandler());
					sql = "SELECT * FROM IOV_VL_DEVICE WHERE ID = " + order.get("IOV_VL_DEVICE_ID") + "";
					Map<String, Object> vlDevice = q.query(conn, sql, new MapHandler());
					BigDecimal fixedType = (BigDecimal) order.get("FIXED_TYPE");
					if (fixedType.intValue() == 1) {
						System.out.println("GPS设备");
						sql = "select * from IOV_BIZ_DEVICE t WHERE t.id=" + vlDevice.get("IOV_BIZ_DEVICE_ID") + "";
						Map<String, Object> bzDevice = q.query(conn, sql, new MapHandler());
						if (bzDevice != null) {
							sql = "select * from HIS_LOCATION t WHERE did = '" + bzDevice.get("DID") + "' and SENDTIME>to_date('2014-06-15','yyyy-MM-dd')";
							List<Map<String, Object>> hisList = q.query(conn, sql, new MapListHandler());
							if (hisList != null && hisList.size() > 0) {
								System.out.println("有[" + hisList.size() + "]个轨迹点");
								boolean b = false;// 是否到圈
								for (Map<String, Object> his : hisList) {
									double dis = calDistance(Double.parseDouble(his.get("LAT").toString()), Double.parseDouble(his.get("LON").toString()), Double.parseDouble(endPlace.get("LAT").toString()), Double.parseDouble(endPlace.get("LON").toString()));
									if (dis < Double.parseDouble(endPlace.get("RADIUS").toString())) {
										b = true;
										System.out.println("[" + his.get("SENDTIME") + "]的时候到圈了");
										break;
									}
								}
							}
						}
					} else if (fixedType.intValue() == 2) {
						System.out.println("手机设备");
					}
				}
			}
		}
	}

	static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	static double round(double d) {
		return Math.floor(d + 0.5);
	}

	/**
	 * @param lat1设备纬度
	 * @param lng1设备经度
	 * @param lat2地图纬度
	 * @param lng2地图经度
	 * @return
	 */
	static double calDistance(double lat1, double lng1, double lat2, double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * 6378137;// 地球半径
		s = round(s * 10000) / 10000;
		return s;
	}

}
