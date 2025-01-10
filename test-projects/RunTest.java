///usr/bin/env jbang "$0" "$@" ; exit $?

//REPOS mavencentral,jitpack
//DEPS org.slf4j:slf4j-simple:1.7.30
//DEPS ${env.CURRENT_WORKFLOW_DEP:dev.snowdrop:buildpack-client:0.0.13-SNAPSHOT}


import java.io.File;
import dev.snowdrop.buildpack.*;
import dev.snowdrop.buildpack.config.*;
import dev.snowdrop.buildpack.docker.*;
import java.util.Map;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;

public class RunTest {

    public static void main(String... args) {

      System.setProperty("org.slf4j.simpleLogger.log.dev.snowdrop.buildpack","debug");
      System.setProperty("org.slf4j.simpleLogger.log.dev.snowdrop.buildpack.docker","debug");
      System.setProperty("org.slf4j.simpleLogger.log.dev.snowdrop.buildpack.lifecycle","debug");
      System.setProperty("org.slf4j.simpleLogger.log.dev.snowdrop.buildpack.lifecycle.phases","debug");

      String projectPath = Optional.of(System.getenv("PROJECT_PATH")).orElse(".");
      String JDK = Optional.of(System.getenv("JDK")).orElse("17");
      String builderImage = Optional.of(System.getenv("BUILDER_IMAGE")).orElse("docker.io/paketocommunity/builder-ubi-base");
      String outputImage = Optional.of(System.getenv("OUTPUT_IMAGE")).orElse("snowdrop/hello-quarkus:jvm"+JDK);

      int exitCode = BuildConfig.builder()
                           .withBuilderImage(new ImageReference(builderImage))
                           .withOutputImage(new ImageReference(outputImage))
                           .withNewPlatformConfig()
                              .withPhaseDebugScript(debugScript)
                              .withEnvironment(Map.of("BP_JVM_VERSION",JDK))
                           .and()
                           .withNewLogConfig()
                              .withLogger(new SystemLogger())
                              .withLogLevel("info")
                           .and()                           
                           .addNewFileContentApplication(new File(projectPath))
                           .build()
                           .getExitCode();

      System.exit(exitCode);
    }
}
