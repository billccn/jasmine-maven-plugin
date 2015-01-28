package com.github.searls.jasmine.thirdpartylibs;

import org.apache.maven.artifact.Artifact;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class ProjectClassLoaderFactory {

  private final Set<Artifact> artifacts;

  public ProjectClassLoaderFactory() {
    this(new HashSet<Artifact>());
  }

  public ProjectClassLoaderFactory(Set<Artifact> artifacts) {
    this.artifacts = artifacts;
  }

  public ClassLoader create() {
    URL[] urls = new URL[artifacts.size()];
    int i = 0;
    try {
      for (Artifact artifact : artifacts) {
        urls[i++] = artifact.getFile().getAbsoluteFile().toURI().toURL();
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
  }
}
