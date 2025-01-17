///usr/bin/env jbang "$0" "$@" ; exit $?

//REPOS mavencentral,jitpack
//DEPS org.slf4j:slf4j-simple:1.7.30
//DEPS ${env.CURRENT_WORKFLOW_DEP}


import java.io.File;
import dev.snowdrop.buildpack.*;
import dev.snowdrop.buildpack.config.*;
import dev.snowdrop.buildpack.docker.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;

public class RunTest {

    public static void main(String... args) {
        try{
            run();
        }catch(Exception e){
            System.err.println("Error during run...");
            e.printStackTrace();
            System.exit(250);
        }
    }

    private static void run() throws Exception {

      System.setProperty("org.slf4j.simpleLogger.log.dev.snowdrop.buildpack","debug");
      System.setProperty("org.slf4j.simpleLogger.log.dev.snowdrop.buildpack.docker","debug");
      System.setProperty("org.slf4j.simpleLogger.log.dev.snowdrop.buildpack.lifecycle","debug");
      System.setProperty("org.slf4j.simpleLogger.log.dev.snowdrop.buildpack.lifecycle.phases","debug");

      String debugScript = "#!/bin/bash\n" +
      "echo \"DEBUG INFO\"\n" +
      "echo \"Root Perms\"\n" +
      "stat -c \"%A $a %u %g %n\" /*\n" +
      "echo \"Layer dir Content\"\n" +
      "ls -lar /layers\n" +
      "echo \"Workspace dir Content\"\n" +
      "ls -lar /workspace\n" +
      "echo \"Analyzed toml\"\n" +
      "ls -la /layers\n" +       
      "cat /layers/analyzed.toml\n" +        
      "LC=$1\n" +
      "shift\n" +
      "$LC \"$@\"";

      String projectPath = Optional.ofNullable(System.getenv("PROJECT_PATH")).orElse(".");
      String JDK = Optional.ofNullable(System.getenv("JDK")).orElse("17");
      String builderImage = Optional.ofNullable(System.getenv("BUILDER_IMAGE")).orElse("docker.io/paketocommunity/builder-ubi-base");
      String outputImage = Optional.ofNullable(System.getenv("OUTPUT_IMAGE")).orElse("snowdrop/hello-quarkus:jvm"+JDK);

      System.out.println("RunTest Building path '"+projectPath+"' using '"+builderImage+"' requesting jdk '"+JDK+"'");

      Map<String,String> envMap = new HashMap<>();
      envMap.put("BP_JVM_VERSION",JDK);

      int exitCode = BuildConfig.builder()
                           .withBuilderImage(new ImageReference(builderImage))
                           .withOutputImage(new ImageReference(outputImage))
                           .withNewPlatformConfig()
                              .withEnvironment(envMap)
                              .withPhaseDebugScript(debugScript)
                           .and()
                           .withNewLogConfig()
                              .withLogger(new SystemLogger())
                              .withLogLevel("debug")
                           .and()                           
                           .addNewFileContentApplication(new File(projectPath))
                           .build()
                           .getExitCode();

      System.exit(exitCode);
    }
}
