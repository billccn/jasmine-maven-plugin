package com.github.searls.jasmine.thirdpartylibs;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.webjars.WebJarAssetLocator;

import com.google.common.io.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.regex.Pattern;

import static org.webjars.WebJarAssetLocator.getFullPathIndex;

public class WebJarResourceHandler extends AbstractThirdPartyLibsResourceHandler {

  public static final String SETUP_SCRIPT_PATH = "webjars.js";

  private final WebJarAssetLocator webJarAssetLocator;
  private final ClassLoader projectClassLoader;
  private byte[] setupScriptBytes;

  public WebJarResourceHandler(ClassLoader projectClassLoader) {
    this.projectClassLoader = projectClassLoader;
    webJarAssetLocator = createWebJarAssetLocator();
  }

  @Override
  protected InputStream findResource(String resourcePath) {
    if (SETUP_SCRIPT_PATH.equals(resourcePath)) {
      return getSetupScriptStream();
    } else {
      return findNormalResource(resourcePath);
    }
  }

  private InputStream findNormalResource(String resourcePath) {
    String fullPath;
    try {
      fullPath = webJarAssetLocator.getFullPath(resourcePath);
    } catch (Exception ignoreToRespondWith404) {
      return null;
    }
    return projectClassLoader.getResourceAsStream(fullPath);
  }

  private InputStream getSetupScriptStream() {
    if (setupScriptBytes == null) {
      try {
        Class<?> cls = new MyClassLoader().loadClass("org.webjars.RequireJS");
        String s = (String) MethodUtils.invokeStaticMethod(cls, "generateSetupJavaScript", Arrays.asList("/webjars/"),
            webJarAssetLocator.getWebJars());
        setupScriptBytes = s.getBytes(StandardCharsets.UTF_8);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    return new ByteArrayInputStream(setupScriptBytes);
  }

  private WebJarAssetLocator createWebJarAssetLocator() {
    SortedMap<String, String> fullPathIndex = getFullPathIndex(Pattern.compile(".*"), projectClassLoader);
    return new WebJarAssetLocator(fullPathIndex);
  }

  class MyClassLoader extends ClassLoader {

    public MyClassLoader() {
      super(projectClassLoader);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      if ("org.webjars.RequireJS".equals(name)) {
        Class<?> orig =  loadClass(name, false);
        if (orig != null) {
          URL url = orig.getClassLoader().getResource("org/webjars/RequireJS.class");
          try {
            byte[] bytes = Resources.toByteArray(url);
            return defineClass("org.webjars.RequireJS", bytes, 0, bytes.length);
          } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + url);
          }
        }
      }
      return super.loadClass(name);
    }
  }
}
