package org.drools.contrib;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * GuvnorJRClusterConfig test case
 */
public class GuvnorJRClusterConfigTest extends BuildFileTest {
    
    @Before
    public void setUp() {
        String path = System.getProperty( "eclipsepath" );
        if ( path == null ) {
            System.setProperty( "eclipsepath",
                                "../../../" );
        }
        configureProject( "src/test/resources/GuvnorJRClusterConfigAntTask.xml" );
    }
   
    @Test 
    public void testConfigurationWasCreated() throws IOException, ClassNotFoundException {
        try {
            executeTarget( "jrcluster" );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( "Should not throw any exception: " + e.getMessage() );
        }
        
        FileInputStream fis1 = new FileInputStream("target/jrcluster/01/repository.xml");
        FileInputStream fis2 = new FileInputStream("target/jrcluster/02/repository.xml");
        
        assertNotNull(fis1);
        assertNotNull(fis2);
    }
}
