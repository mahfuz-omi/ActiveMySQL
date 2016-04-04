/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package activemysql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author omi
 */
public class Select {
    
    private boolean isWhereEnabled;
    private boolean isLimitEnabled;
    private String whereClause;
    private int limitValue;
    private String selectColumns;
    private String tableName;
    
    
    public Class<?> className;
    
    private String databaseName;
    private String userName;
    private String password;
    
    
    //instance initializer...called before constructor
    {
    
    
          Annotation[] annotations = className.getAnnotations();

          for(Annotation annotation : annotations)
          {
            if(annotation instanceof DBSettings)
            {
                DBSettings myAnnotation = (DBSettings) annotation;
                this.databaseName = myAnnotation.databaseName();
                this.userName = myAnnotation.userName();
                this.password = myAnnotation.password();
                
                System.out.println("name: " + myAnnotation.databaseName());
                System.out.println("value: " + myAnnotation.userName());
                System.out.println("value: " + myAnnotation.password());
            }
          }
    
    }
    
    private String functionName(String para)
    {
        String first = String.valueOf(para.charAt(0));
        first = first.toUpperCase();
        String a = "set"+first + para.substring(1);
        return a;
    }
    
    public Select(String selectColumns)
    {
        this.selectColumns = " "+selectColumns;
    }
    
    public Select(List<String> columns)
    {
        String p = " ";
        for(String col:columns)
        {
            p = p + "'"+col+"'"+",";
        }
    }
    
    
    
    public Select from(Class<?> className) 
    {
        this.className = className;
        this.tableName = className.getSimpleName();
        return this;
    }
    
    public Select where(String []columns,String []values) throws ActiveMySQLException
    {
        if(columns.length != values.length)
        {
            throw new ActiveMySQLException("error in while input parameter");    
        }
        this.isWhereEnabled = true;
        StringBuffer where = new StringBuffer();
        for(int i=0;i<columns.length;i++)
        {
            where.append(columns[i]+"="+values[i]);
            if(i < columns.length-1)
            {
                where.append(" ");
                where.append("and");
                where.append(" ");
            }
            
        }
        this.whereClause = where.toString();
        System.out.println("omi "+where.toString());
        
        return this;   
    }
    
    
    public List<Object> execute()
    {
        
        ArrayList<Object> list = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        sql.append(this.selectColumns);
        sql.append(" FROM");
        sql.append(" ");
        sql.append(this.tableName);
        sql.append(" ");
        if(this.isWhereEnabled)
        {
            sql.append(" WHERE ");
            sql.append(this.whereClause);
        }
        sql.append(" ");
        if(this.isLimitEnabled)
        {
            sql.append(" LIMIT ");
            sql.append(this.limitValue+"");
        }
        sql.append(";");
        String finalSQL = sql.toString();
        System.out.println("in execute:"+finalSQL);
        
        Connection c = null;
        Statement stmt = null;
        try 
        {
          Class.forName("com.mysql.jdbc.Driver");
          c = DriverManager.getConnection("jdbc:mysql://localhost/" + this.databaseName + "?" + "user=" + this.userName + "&password=" + this.password);
          
          stmt = c.createStatement();
   
          ResultSet rs = stmt.executeQuery(finalSQL);
          ResultSetMetaData data = rs.getMetaData();
          
         
          while(rs.next()) 
          {
                Object obj = null;
                obj = className.getConstructor().newInstance();

                for(int i=2;i<=data.getColumnCount();i++)
                {
                    String colName = data.getColumnName(i);
                    String colType = data.getColumnTypeName(i);
                    Field f = className.getDeclaredField(colName);
                    f.setAccessible(true);
                    if(colType.equalsIgnoreCase("VARCHAR"))
                    {
                        String val = rs.getString(colName);
                        className.getMethod(functionName(colName), String.class).invoke(obj,val);
                        //f.set(obj, f.);
                        
                        
                    }
                    else if(colType.equalsIgnoreCase("INT"))
                    {
                        int val = rs.getInt(colName);
                        className.getMethod(functionName(colName), int.class).invoke(obj,val);
                    }
                    else if(colType.equalsIgnoreCase("DOUBLE"))
                    {
                        double val = rs.getDouble(colName);
                        className.getMethod(functionName(colName), double.class).invoke(obj,val);
                    }

                }
                list.add(obj);
           
          }
          
          stmt.close();
          c.close();
        } 
        catch ( Exception e ) 
        {
          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
          System.exit(0);
        }
        return list;
    }
    
    
    public Select limit(int limitValue)
    {
        this.isLimitEnabled = true;
        this.limitValue = limitValue;
        return this;
    }
    
}
