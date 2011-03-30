package org.drools.contrib;

import java.io.File;
import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.RepositoryCopier;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Ant task to help backup and migrate jackrabbit repo.
 */
public class JackrabbitMigrationAntTask extends MatchingTask {
    private String verbose;
    private boolean verboseVal;
    private String sourcedir;
    private String sourceconfig;
    private String targetdir;
    private String targetconfig;
    
    /**
     * Task's main method
     */
    public void execute() throws BuildException {
        super.execute();
        verboseVal = verbose != null && verbose.equalsIgnoreCase("true");
        
        String inputErrors = checkInput();
        if( inputErrors.length() >  0) {
            throw new BuildException(inputErrors);
        }
        
        try {
            if (verboseVal) {
                log("Installing source repository.");
            }
            RepositoryConfig sourceRepo = RepositoryConfig.install(
                    new File(sourceconfig), new File(sourcedir));
            if (verboseVal) {
                log("Installing target repository.");
            }
            RepositoryConfig targetRepo = RepositoryConfig.install(
                        new File(targetconfig), new File(targetdir));
                if (verboseVal) {
                    log("Migrating source repository to target repository.");
                }
                RepositoryCopier.copy(sourceRepo, targetRepo);
        } catch (Exception e) {
            log(e.getMessage());
            throw new BuildException(e);
        }
    }
    
    public void setVerbose(String verbose) {
        this.verbose = verbose;
    }

    private String checkInput() {
        if(verboseVal) {
            log("Validating task input parameters.");
        }
        String errors = "";
        
        if(isEmpty(sourcedir)) {
            errors += "\nInvalid source repository directory.";
        } else {
            File sourceDirFile = new File(sourcedir);
            if(!sourceDirFile.canRead()) {
                errors += "\nInvalid source repository directory.";
            }
        }
        
        if(isEmpty(sourceconfig)) {
            errors += "\nInvalid source source configuration.";
        } else {
            File sourceConfFile = new File(sourceconfig);
            if(!sourceConfFile.canRead()) {
                errors += "\nInvalid source repository configuration file.";
            }
        }
        
        if(isEmpty(targetconfig)) {
            errors += "\nInvalid target repository configuration.";
        } else {
            File targetConfFile = new File(targetconfig);
            if(!targetConfFile.canRead()) {
                errors += "\nInvalid target repository configuration file.";
            }
        }
        
        if(isEmpty(targetdir)) {
            errors += "\nInvalid target directory configuration.";
        } else {
            File targetDirFile = new File(targetdir);
            if(!targetDirFile.canRead()) {
                errors += "\nInvalid target repository directory.";
            }
        }
        
        return errors;
    }
    
    public static boolean isEmpty(final CharSequence str) {
        if ( str == null || str.length() == 0 ) {
            return true;
        }
        
        for ( int i = 0, length = str.length(); i < length; i++ ){
            if ( str.charAt( i ) != ' ' )  {
                return false;
            }
        }
        
        return true;
    }

    public void setSourcedir(String sourcedir) {
        this.sourcedir = sourcedir;
    }
    
    public void setSourceconfig(String sourceconfig) {
        this.sourceconfig = sourceconfig;
    }

    public void setTargetdir(String targetdir) {
        this.targetdir = targetdir;
    }

    public void setTargetconfig(String targetconfig) {
        this.targetconfig = targetconfig;
    }

}
