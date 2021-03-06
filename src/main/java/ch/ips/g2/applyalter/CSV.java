package ch.ips.g2.applyalter;

import au.com.bytecode.opencsv.CSVReader;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;

/**
 * SQL statement in Alter script
 *
 * @author Martin Caslavsky &lt;martin.caslavsky@ips-ag.cz&gt;
 * @version $Id$
 */
@XStreamAlias("csv")
public class CSV extends AbstractStatement {
    protected String file;
    protected Integer step = null;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public CSV() {
        super();
    }


    /**
     * Sql statement is just the statement itself.
     */
    public String getSqlStatement() {
        return getStatement();
    }

    public void execute(DbInstance dbConn, RunContext ctx, Map<String, byte[]> datafiles)
            throws ApplyAlterException, SQLException {
        Connection connection = dbConn.getConnection(ctx);
        String sql = getSqlStatement().trim();

        final byte[] rawFile = datafiles.get(getFile());
        if (rawFile == null) {
            throw new ApplyAlterException(String.format("missing top-level element: <datafile>%s</datafile>", getFile()));
        }

        CSVReader rdr = new CSVReader(new InputStreamReader(new ByteArrayInputStream(rawFile), Charset.forName("UTF-8")));

        PreparedStatement st = null;
        try {
            final String[] headRow = rdr.readNext();
            final int numParams = headRow.length;
            ctx.report(ReportLevel.STATEMENT_STEP, "CSV columns:%s%n", Arrays.asList(headRow));

            //hack: any LOBs must be _after_ CSV columns
            st = prepareStatement(connection, sql, datafiles, numParams);

            final ParameterMetaData paramTypes = st.getParameterMetaData();
            if (paramTypes.getParameterCount() != numParams) {
                throw new ApplyAlterException(String.format("invalid CSV: %d columns for %d query parameters",
                        numParams, paramTypes.getParameterCount()));
            }

            int rows = 0;
            int execCnt = 0;
            final Integer step = getStep();

            String[] row;
            while ((row = rdr.readNext()) != null) {
                //fill parameters
                for (int paramIdx = 1; paramIdx <= numParams; paramIdx++) {
                    String paramVal = row[paramIdx - 1];
                    fillParam(st, paramTypes, paramIdx, paramVal);
                }

                //execute
                if (!st.execute()) // allows "with ... select ... update ..."
                {
                    rows += st.getUpdateCount();
                }
                execCnt++;

                if (step != null && step.intValue() > 0 && (execCnt % step) == 0) {
                    commitStep(ctx, connection);
                }

            }

            ctx.report(ReportLevel.STATEMENT_STEP, "statement executed %d times, changed rows: %d%n", execCnt, rows);
            rdr.close();
        } catch (IOException e) {
            throw new ApplyAlterException("error reading CSV file " + getFile(), e);
        } finally {
            DbUtils.close(st);
        }

    }

    /**
     * Check the value type, parse value from CSV and call appropriate setXXX method.
     */
    private void fillParam(PreparedStatement st, ParameterMetaData paramTypes, int paramIdx, String paramVal)
            throws SQLException {
        final int type = paramTypes.getParameterType(paramIdx);
        //string types are special: empty string is not null!
        switch (type) {
            case Types.VARCHAR:
            case Types.CLOB:
            case Types.CHAR:
                st.setString(paramIdx, paramVal);
                return;
        }
        //in other cases, empty string is considered NULL
        if (StringUtils.isEmpty(paramVal)) {
            st.setNull(paramIdx, type);
            return;
        }

        switch (type) {
            case Types.INTEGER:
                st.setInt(paramIdx, Integer.parseInt(paramVal));
                break;
            case Types.BIGINT:
                st.setLong(paramIdx, Long.parseLong(paramVal));
                break;
            case Types.SMALLINT:
                st.setShort(paramIdx, Short.parseShort(paramVal));
                break;
            case Types.DECIMAL:
            case Types.DOUBLE:
                st.setDouble(paramIdx, Double.parseDouble(paramVal));
                break;
            case Types.FLOAT:
                st.setFloat(paramIdx, Float.parseFloat(paramVal));
                break;
            default:
                throw new ApplyAlterException("unsupported type in CSV: " + paramTypes.getParameterTypeName(paramIdx));
        }
    }

}