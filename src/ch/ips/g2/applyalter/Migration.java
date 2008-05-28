package ch.ips.g2.applyalter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Migration script, which wouldnt fit to one transaction
 * database/alter2/b_7.1/nas_7.1/alter-common_fn.sql
 * database/alter2/b_7.3/nas_7.3.1/alter-common_fn.sql
 * 
 * @author Martin Caslavsky &lt;martin.caslavsky@ips-ag.cz&gt;
 * @version $Id$
 */
@XStreamAlias("migration")
public class Migration extends AbstractStatement
{
  
  public String logid;
  public Integer maxblkcnt;
  public String description;
  public Integer fromid;
  public Integer toid;
  public Integer step;

  public Migration() {
    super();
  }

  public Migration(String logid, Integer maxblkcnt, String description, String statement) {
    this();
    this.logid = logid;
    this.maxblkcnt = maxblkcnt;
    this.description = description;
    super.statement = statement;
  }

  public Migration(String logid, Integer maxblkcnt, String description,
      String statement, Integer fromid, Integer toid, Integer step) {
    super();
    this.logid = logid;
    this.maxblkcnt = maxblkcnt;
    this.description = description;
    super.statement = statement;
    this.fromid = fromid;
    this.toid = toid;
    this.step = step;
  }

  @Override
  public String getSQLStatement()
  {
    return isFt() ? "call g2fn.blockupdate(?,?,?,?)" : "call g2fn.blockupdate_ft(?,?,?,?,?,?,?)";
  }

  @Override
  public PreparedStatement getPreparedStatement(Connection con)
      throws SQLException
  {
    PreparedStatement s = con.prepareStatement(getSQLStatement());
    int i = 1;
    s.setString(i++, logid);
    s.setInt(i++, maxblkcnt);
    s.setString(i++, description);
    s.setString(i++, statement);
    if (isFt()) {
      s.setInt(i++, fromid);
      s.setInt(i++, toid);
      s.setInt(i++, step);      
    }
    return s;
  }
  
  /**
   * Is this blockupdate_ft() or blockupdate()?
   * @return true if blockupdate_ft()
   * @throws IllegalArgumentException if any of required parameter is missing
   */
  public boolean isFt() throws IllegalArgumentException {
    if (logid != null && maxblkcnt != null && description != null && statement != null && 
        fromid != null && toid != null && step != null)
      return true;
    else if (logid != null && maxblkcnt != null && description != null && statement != null)
      return false;
    else
      throw new IllegalArgumentException("Incorrectly filled parameters");
  }

  public String getLogid()
  {
    return logid;
  }

  public void setLogid(String logid)
  {
    this.logid = logid;
  }

  public Integer getMaxblkcnt()
  {
    return maxblkcnt;
  }

  public void setMaxblkcnt(Integer maxblkcnt)
  {
    this.maxblkcnt = maxblkcnt;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public Integer getFromid()
  {
    return fromid;
  }

  public void setFromid(Integer fromid)
  {
    this.fromid = fromid;
  }

  public Integer getToid()
  {
    return toid;
  }

  public void setToid(Integer toid)
  {
    this.toid = toid;
  }

  public Integer getStep()
  {
    return step;
  }

  public void setStep(Integer step)
  {
    this.step = step;
  }

}
