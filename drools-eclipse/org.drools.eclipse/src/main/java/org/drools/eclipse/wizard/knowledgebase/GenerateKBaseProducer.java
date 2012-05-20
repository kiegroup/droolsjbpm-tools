package org.drools.eclipse.wizard.knowledgebase;

import java.io.InputStream;

public class GenerateKBaseProducer {
    public static String generateQualifier(String pkgName, String kbaseName) {
        String s = "package " + pkgName + ";\n"+
                "import static java.lang.annotation.ElementType.TYPE;\n" + 
                "import static java.lang.annotation.ElementType.FIELD;\n" + 
                "import static java.lang.annotation.ElementType.PARAMETER;\n" + 
                "import static java.lang.annotation.ElementType.METHOD;\n" + 
                "import java.lang.annotation.Retention;\n" + 
                "import java.lang.annotation.RetentionPolicy;\n" + 
                "import java.lang.annotation.Target;\n" + 
                "import javax.inject.Qualifier;" +
                "@Retention(RetentionPolicy.RUNTIME)\n" + 
                "@Target({FIELD,METHOD,PARAMETER,TYPE})\n" + 
                "@Qualifier\n" + 
                "public @interface " + kbaseName +" {\n" + 
                "\n" + 
                "}\n" + 
                "";        
        return s;
    }
    public static String generateProducer(String pkgName, String kbaseName) {                
        String s = "package " + pkgName + ";\n" +
                   "import java.util.Properties;\n" +
                   "import java.io.IOException;\n" + 
                   "import java.io.InputStream;\n" +
                   "import javax.enterprise.inject.Produces;\n" + 
                   "import javax.inject.Named;\n" + 
                   "import org.drools.KnowledgeBase;\n" + 
        		   "import org.drools.KnowledgeBaseFactory;\n" + 
        		   "import org.drools.builder.CompositeKnowledgeBuilder;\n" + 
        		   "import org.drools.builder.KnowledgeBuilder;\n" + 
        		   "import org.drools.builder.KnowledgeBuilderFactory;\n" + 
        		   "import org.drools.builder.ResourceType;\n" + 
        		   "import org.drools.io.ResourceFactory;\n" +
        		   "public class " + kbaseName + "Producer {\n" +
        		   "    @Produces \n" +
        		   "    @" + kbaseName + "\n"+
        		   "    public KnowledgeBase newKnowledgeBase() {\n" +
        		   "        Properties props = new Properties();\n" +
        		   "        InputStream is = null;\n" + 
        		   "        try {\n" +
        		   "            is = Class.class.getResourceAsStream( \"/" + kbaseName + ".properties\" );\n" + 
        		   "            props.load( is );\n" + 
        		   "        } catch ( IOException e ) {\n" + 
        		   "            throw new RuntimeException( \"Unable to fine files for KnowledgeBase " + kbaseName + "\" );\n" + 
        		   "        } finally {\n" +
        		   "            if ( is != null ) {\n" + 
        		   "                try {\n" + 
        		   "                    is.close();\n" + 
        		   "                } catch (IOException e) {\n" +  
        		   "                    e.printStackTrace();\n" + 
        		   "                }\n" + 
        		   "            }\n" +
        		   "        }\n" + 
        		   "        \n" + 
        		   "        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();\n" + 
        		   "        CompositeKnowledgeBuilder ckbuilder = kbuilder.batch();\n" + 
        		   "        \n" + 
        		   "        String fileStr = ( String ) props.get( \"files\" );\n" + 
        		   "        String[] files = fileStr.split( \",\" );\n" + 
        		   "        if ( files.length > 0 ) {\n" + 
        		   "            for ( String file : files ) {\n" + 
        		   "                if ( file.endsWith(\".drl\" ) ) {\n" +
        		   "                    ckbuilder.add( ResourceFactory.newUrlResource( Class.class.getResource( \"/\" + file.trim() ) ), ResourceType.DRL );\n" +
        		   "                }\n" + 
        		   "            }\n" + 
        		   "        }\n" + 
        		   "        ckbuilder.build();\n" + 
        		   "\n" + 
        		   "        \n" + 
        		   "        if ( kbuilder.hasErrors() ) {\n" + 
        		   "            throw new RuntimeException( \"Unable to compile " + kbaseName + ":\\n\" + kbuilder.getErrors() );\n" + 
        		   "        }\n" + 
        		   "        \n" + 
        		   "        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();\n" + 
        		   "        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );\n" +
        		   "        return kbase; \n" +
        		   "    }\n" +
        		   "}\n";
                       
        return s;
        
    }
}
