package org.rex.db.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.rex.db.datasource.pool.SimpleConnectionPool;
import org.rex.db.exception.DBException;
import org.rex.db.exception.DBRuntimeException;

/**
 * 框架内置的简单数据源，内置一个简单的连接池，通常用于开发环境。
 * 
 */
public class SimpleDataSource implements DataSource {

	private final SimpleConnectionPool pool;
	
	public SimpleDataSource(Properties properties) throws DBException {
		pool = new SimpleConnectionPool(properties);
	}

	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		throw new DBRuntimeException("DB-D0002", "getConnection");
	}

	public int getLoginTimeout() throws SQLException {
		throw new DBRuntimeException("DB-D0002", "getLoginTimeout");
	}

	public void setLoginTimeout(int timeout) throws SQLException {
		throw new DBRuntimeException("DB-D0002", "setLoginTimeout");
	}

	public PrintWriter getLogWriter() {
		throw new DBRuntimeException("DB-D0002", "getLogWriter");
	}

	public void setLogWriter(PrintWriter pw) throws SQLException {
		throw new DBRuntimeException("DB-D0002", "setLogWriter");
	}

}
