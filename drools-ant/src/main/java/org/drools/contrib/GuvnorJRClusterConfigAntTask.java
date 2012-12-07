package org.drools.contrib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * Ant task to help configure JR cluster for a set of Guvnor nodes.
 */
public class GuvnorJRClusterConfigAntTask  extends MatchingTask {
    private String verbose; //optional
    private String nodecount;
    private String nodenames; //optional
    private String destinationdir;
    private String dbtype;
    private String dbdriver;
    private String dburl;
    private String dbuser;
    private String dbpasswd;
    private String journalsyncdelay;
    private String journaldbdriver;
    private String journaldburl;
    private String journaldbuser;
    private String journaldbpasswd;
  
    private List<String> nodes = new ArrayList<String>();
    private Map<String, Map<String, Object>> repoTemplateDataMap = new HashMap<String, Map<String,Object>>();
    private List<String> availDbTypes = Arrays.asList(new String[] {"mssql", "mysql", "oracle10g", "oracle11", "oracle9i", "postgresql"});
    private int nodeCountVal;
    private int journalSyncDelayVal;
    private boolean verboseVal;
    
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
        
        createConfigDirectoryStructure();
        setupRepositoryTemplateData();
        Iterator<Entry<String, Map<String, Object>>> iter =  repoTemplateDataMap.entrySet().iterator();
        while(iter.hasNext()) {
            Entry<String, Map<String, Object>> entry = iter.next();
            createConfigEntries(entry.getKey(), entry.getValue());
        }
        
        
    }
    
    private void createConfigEntries(String node, Map<String, Object> data) {
        String repositoryConfig = processTemplate(dbtype+"-repository.xml", data);
        // write configs to file system
        try {
            File repoFile = new File(destinationdir + "jrcluster/" + node + "/repository.xml");
            if(!repoFile.exists()) {
                boolean success = repoFile.createNewFile();
                if(!success) throw new BuildException("Unable to create file [" + repoFile.getPath() + "]");
            }
            FileOutputStream fosr = new FileOutputStream(repoFile);
            fosr.write(repositoryConfig.getBytes());
            fosr.flush();
            fosr.close();
        } catch (Exception e) {
            throw new BuildException(e.getMessage());
        }
    }
    
    private void setupRepositoryTemplateData() {
        for(String node : nodes) {
            Map<String, Object> nodeMap = new HashMap<String, Object>();
            nodeMap.put("nodeID", node);
            nodeMap.put("dbType", dbtype);
            nodeMap.put("dbDriver", dbdriver);
            nodeMap.put("dbUrl", dburl);
            nodeMap.put("dbUser", dbuser);
            nodeMap.put("dbPass", dbpasswd);
            nodeMap.put("journalSyncDelay", journalsyncdelay);
            nodeMap.put("journalDriver", journaldbdriver);
            nodeMap.put("journalUrl", journaldburl);
            nodeMap.put("journalUser", journaldbuser);
            nodeMap.put("journalPass", journaldbpasswd);
            
            repoTemplateDataMap.put(node, nodeMap);
        }
    }
    
    private void createConfigDirectoryStructure() {
        // create top-level configuration directory under destinationDir
        if(!destinationdir.endsWith("/")) destinationdir = destinationdir + "/";
        boolean success = (new File(destinationdir + "jrcluster")).mkdir();
        if (!success) {
            throw new BuildException("Unable to create directory: [" + destinationdir + "jrcluster/]");
        }
        
        // now the node directories
        for(int i = 0; i < nodeCountVal; i++) {
            success = (new File(destinationdir + "jrcluster/" + nodes.get(i) )).mkdirs();
            if (!success) {
                throw new BuildException("Unable to create directory: [" + destinationdir + "jrcluster/" + nodes.get(i) + "]");
            }
        }
    }
    
    private String checkInput() {
        String errors = "";
        if(nodecount == null) {
            errors += "\nInvalid node count.";
        }
        try {
            nodeCountVal = Integer.parseInt(nodecount);
            if(nodeCountVal <= 0) {
                errors += "\nNode count must be greater than zero.";
            }
        } catch (NumberFormatException e) {
            errors += "\nNode count is not a number";
        }
        if(destinationdir == null) {
            errors += "\nInvalid destination directory.";
        }
        if(dbtype == null) {
            errors += "\nInvalid database type.";
        }
        if(!availDbTypes.contains(dbtype)) {
            errors += "\nInvalid db type [" + dbtype + "].";
        }
        if(dbdriver == null) {
            errors += "\nInvalid database driver.";
        }
        if(dburl == null) {
            errors += "\nInvalid database url.";
        }
        if(dbuser == null) {
            errors += "\nInvalid database user.";
        }
        if(dbpasswd == null) {
            errors += "\nInvalid database password.";
        }
        if(journalsyncdelay == null) {
            errors += "\nInvalid journal sync delay.";
        }
        try {
            journalSyncDelayVal = Integer.parseInt(journalsyncdelay);
            if(journalSyncDelayVal <= 0) {
                errors += "\nJournal sync delay must be greater than zero.";
            }
        } catch (NumberFormatException e) {
            errors += "\nJournal sync delay is not a number";
        }
        if(journaldbdriver == null) {
            errors += "\nInvalid journal database driver.";
        }
        if(journaldburl == null) {
            errors += "\nInvalid journal database url.";
        }
        if(journaldbuser == null) {
            errors += "\nInvalid journal database user.";
        }
        if(journaldbpasswd == null) {
            errors += "\nInvalid journal database password.";
        }
        
        if(nodenames != null) {
            nodes =  Arrays.asList(nodenames.split(","));
            if(nodes.size() != nodeCountVal) {
                errors += "\nNumber of node names [" + nodes.size() + "] does not match given node count [" + nodecount + "]";
            }
        } else {
            for(int i=0; i < nodeCountVal; i++) {
                if(i < 10 ) {
                    nodes.add("0" + (i+1));
                } else {
                    nodes.add( (i+1) + "");
                }
            }
        }
        
        return errors;
    }
    
    private String processTemplate(String name, Map<String, Object> data) {
        if(verboseVal) {
            log("Processing template [" + name + "] with data: ");
            for(String key : data.keySet()) {
                log("\t" + key + " - " + data.get(key));
            }
        }
        try {
            Configuration cfg = new Configuration();
            cfg.setObjectWrapper( new DefaultObjectWrapper() );
            cfg.setTemplateUpdateDelay( 0 );
            
            Template temp = new Template( name,
                    new InputStreamReader( GuvnorJRClusterConfigAntTask.class.getResourceAsStream( "/jrcluster/templates/repository/" + name ) ),
                    cfg );
            StringWriter strw = new StringWriter();
            temp.process( data, strw );
            return strw.toString();
        } catch (Exception e) {
            log("Exception processing template: " + e.getMessage());
            return null;
        }
    }

    public void setNodecount(String nodecount) {
        this.nodecount = nodecount;
    }

    public void setNodenames(String nodenames) {
        this.nodenames = nodenames;
    }

    public void setDestinationdir(String destinationdir) {
        this.destinationdir = destinationdir;
    }
    public void setDbtype(String dbtype) {
        this.dbtype = dbtype;
    }

    public void setDbdriver(String dbdriver) {
        this.dbdriver = dbdriver;
    }

    public void setDburl(String dburl) {
        this.dburl = dburl;
    }

    public void setDbuser(String dbuser) {
        this.dbuser = dbuser;
    }

    public void setDbpasswd(String dbpasswd) {
        this.dbpasswd = dbpasswd;
    }

    public void setJournaldbdriver(String journaldbdriver) {
        this.journaldbdriver = journaldbdriver;
    }

    public void setJournaldburl(String journaldburl) {
        this.journaldburl = journaldburl;
    }

    public void setJournaldbuser(String journaldbuser) {
        this.journaldbuser = journaldbuser;
    }

    public void setJournaldbpasswd(String journaldbpasswd) {
        this.journaldbpasswd = journaldbpasswd;
    }
    
    public void setJournalsyncdelay(String journalsyncdelay) {
        this.journalsyncdelay = journalsyncdelay;
    }

    public void setVerbose(String verbose) {
        this.verbose = verbose;
    }
    
}
