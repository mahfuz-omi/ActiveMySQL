/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package activemysql;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author omi
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DBSettings {
    
    String databaseName()default "Test";
    String userName() default "root";
    String password() default "";
}
