package com.example.batch.config;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class TrackedDataSource implements DataSource {
	
	private DataSource dataSource;
    private List<Connection> trackedConnections;
    
	public TrackedDataSource(DataSource dataSource) {
		// TODO Auto-generated constructor stub
		this.dataSource = dataSource;
		this.trackedConnections = new ArrayList<>();
	}

	@Override
    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        trackedConnections.add(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = dataSource.getConnection(username, password);
        trackedConnections.add(connection);
        return connection;
    }
    
    public List<Connection> getAllConnections() {
        return trackedConnections;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return dataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return dataSource.isWrapperFor(iface);
	}

}
