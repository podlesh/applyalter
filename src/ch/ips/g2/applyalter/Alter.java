package ch.ips.g2.applyalter;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.InputSource;

import ch.ips.base.BaseXMLDeserializer;

/**
 * This class holds alter script (list of alter statements) which
 * are intended to be applied to a set of database instances or to all of them. 
 * Class is usually loaded from XML.
 * @author Martin Caslavsky &lt;martin.caslavsky@ips-ag.cz&gt;
 * @version $Id$
 */
public class Alter
{
  private String id;
  public Set<DbInstanceType> instances = new HashSet<DbInstanceType>();
  public List<AlterStatement> statements = new ArrayList<AlterStatement>();
  public String check;
  
  public Alter() {
    super();
  }
  public Alter(Set<DbInstanceType> instance, List<AlterStatement> statements) {
    this();
    this.instances = instance;
    this.statements = statements;
  }
  public Alter(Set<DbInstanceType> instances, String check, AlterStatement... statements) {
    this();
    this.instances = instances;
    this.check = check;
    addStatements(statements);
  }
  public Alter(DbInstanceType instance, String check, AlterStatement... statements) {
    this();
    this.instances.add(instance);
    this.check = check;
    addStatements(statements);
  }
  
  public void addStatements(AlterStatement... statements)
  {
    for (AlterStatement i: statements)
      this.statements.add(i);
  }
  public List<AlterStatement> getStatements()
  {
    return statements;
  }
  public void setStatements(List<AlterStatement> statements)
  {
    this.statements = statements;
  }
  public Set<DbInstanceType> getInstances()
  {
    return instances;
  }
  public void setInstances(Set<DbInstanceType> instances)
  {
    this.instances = instances;
  }
  /**
   * Apply this alter to all instances?
   * @return true if so, false otherwise
   */
  public boolean isAllInstances() {
    return instances == null || instances.isEmpty();
  }
  public String getId()
  {
    return id;
  }
  public void setId(String id)
  {
    this.id = id;
  }
  
  public String getCheck()
  {
    return check;
  }
  public void setCheck(String check)
  {
    this.check = check;
  }
  /**
   * Create new instance from XML serialized form
   * @param file identifier for {@link Alter#setId()}
   * @param i input stream to read from
   * @return new instance
   */
  public static Alter newInstance(String file, InputStream i)
  {
    Alter a = (Alter) new BaseXMLDeserializer().fromXML(new InputSource(i));
    if (a == null)
      throw new ApplyAlterException("Unable to deserialize Alter from file " + file);
    // get file name part
    a.setId(new File(file).getName());
    return a;
  }


  
}
