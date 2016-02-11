package org.rex.db.core.statement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.rex.db.Ps;
import org.rex.db.dialect.LimitHandler;
import org.rex.db.exception.DBException;
import org.rex.db.util.SqlUtil;

/**
 * Statement Creator for Ps
 * 
 * Set standard SQL prepared statement parameters. such as:
 * sql: INSERT INTO TBL(CL1, CL2) VALUES(?,?)
 * parameters: new Ps("100", "M");
 */
public class PsStatementCreator extends AbstractStatementCreator{
	
	//----------Prepared Statement
	public PreparedStatement createPreparedStatement(Connection connection, String sql, Object parameters) throws DBException, SQLException {
		return createPreparedStatement(connection, sql, parameters, null);
	}
	
	public PreparedStatement createPreparedStatement(Connection connection, String sql, Object parameters, LimitHandler limitHandler)
			throws DBException, SQLException {
		return createPreparedStatement(connection, sql, (Ps)parameters, limitHandler);
	}
	
	private PreparedStatement createPreparedStatement(Connection conn, String sql, Ps ps, LimitHandler limitHandler) throws DBException, SQLException{
		if(limitHandler != null)
			sql = limitHandler.wrapSql(sql);
		
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		if(ps != null)
			setParameters(preparedStatement, ps);
		
		if(limitHandler != null)
			limitHandler.afterSetParameters(preparedStatement, ps == null ? 0 : ps.getParameterSize());
		
		return preparedStatement;
	}
	
	//----------Callable Statement
	public CallableStatement createCallableStatement(Connection connection, String sql, Object parameters) throws DBException, SQLException {
		return createCallableStatement(connection, sql, (Ps)parameters);
	}
	
	private CallableStatement createCallableStatement(Connection conn, String sql, Ps ps) throws SQLException {
		CallableStatement cs = conn.prepareCall(sql);
		
		if(ps == null) return cs;
		
		List<Ps.SqlParameter> parameters = ps.getParameters();
		for (int i = 0; i < parameters.size(); i++) {
			Ps.SqlParameter parameter = parameters.get(i);
			
			//out parameters
			if(parameter instanceof Ps.SqlOutParameter) {
				String paramName = ((Ps.SqlOutParameter<?>) parameter).getParamName();
				if (paramName != null) {
					cs.registerOutParameter(i + 1, parameter.getSqlType(), paramName);
				}else {
					cs.registerOutParameter(i + 1, parameter.getSqlType());
				}
			}
			
			//in and inout parameters
			if(!(parameter instanceof Ps.SqlOutParameter) || parameter instanceof Ps.SqlInOutParameter){
				SqlUtil.setParameter(cs, i + 1, parameter.getValue(), parameter.getSqlType());
			}
		}
		return cs;
	}
	
	//----------Batch Prepared Statement
	public PreparedStatement createBatchPreparedStatement(Connection connection, String sql, Object[] parametersArray)
			throws DBException, SQLException {
		return createBatchPreparedStatement(connection, sql, (Ps[])parametersArray);
	}
	
	private PreparedStatement createBatchPreparedStatement(Connection conn, String sql, Ps[] ps) throws DBException, SQLException {
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		
		if(ps != null){
	        for(int i = 0; i < ps.length; i++){
	        	if(ps[i] != null){
	        		setParameters(preparedStatement, ps[i]);
	        		preparedStatement.addBatch();
	        	}
	        }
		}
		
        return preparedStatement;
	}
	
	//------private methods
	/**
	 * set parameters for PreparedStatement
	 */
	private void setParameters(PreparedStatement preparedStatement, Ps ps) throws DBException, SQLException{
		List<Ps.SqlParameter> parameters = ps.getParameters();

		for (int i = 0; i < parameters.size(); i++) {
			Ps.SqlParameter parameter = parameters.get(i);
			if(parameter instanceof Ps.SqlOutParameter)
				throw new DBException("DB-C0001", i, parameters, ps);
			
			SqlUtil.setParameter(preparedStatement, i + 1, parameter.getValue(), parameter.getSqlType());
		}
	}


}