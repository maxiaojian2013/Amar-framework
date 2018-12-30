package com.amarsoft.coderj.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassLoaderUtil {
	private static final Logger LOGGER=LoggerFactory.getLogger(ClassLoaderUtil.class);
	
	private static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
	
public static Set<Class<?>> getClassSet(String packageName) {
	Set<Class<?>> classes=new HashSet<Class<?>>();
	String packageAbsPath=packageName.replace(".","/");
	try {
		Enumeration<URL> enumeration= getClassLoader().getResources(packageAbsPath);
		while (enumeration.hasMoreElements()) {
			URL url = (URL) enumeration.nextElement();
			String protocol=url.getProtocol();
			String path=url.getPath();
			if (protocol=="file") {
				addClass(classes, path, packageName);
			}
			if (protocol=="jar") {
				 JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                 if (jarURLConnection != null) {
                     JarFile jarFile = jarURLConnection.getJarFile();
                     if (jarFile != null) {
                         Enumeration<JarEntry> jarEntries = jarFile.entries();
                         while (jarEntries.hasMoreElements()) {
                             JarEntry jarEntry = jarEntries.nextElement();
                             String jarEntryName = jarEntry.getName();
                             if (jarEntryName.endsWith(".class")) {
                                 String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                 Class<?> clazz;
								try {
									clazz = getClassLoader().loadClass(className);
									classes.add(clazz);
								} catch (ClassNotFoundException e) {
									LOGGER.error("类不存在");
									e.printStackTrace();
								}
                                 
                             }
			}
		}
                 }}}} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return classes;
}

private static void addClass(Set<Class<?>> classSet,String packagePath,String packageName) {
	File [] files=new File(packagePath).listFiles(new FileFilter() {
		public boolean accept(File file) {
			// TODO Auto-generated method stub
			return (file.isFile()&&file.getName().endsWith(".class"))||file.isDirectory();
		}
	});
	
	for (File file : files) {
		//.calss结尾的。取其包名+‘。’+类名。添加到set
		String fileName=file.getName();
		if (file.isFile()) {
		   String className=fileName.substring(0, fileName.indexOf("."));
		   String fullClassName=packageName+"."+className;
		   Class<?> clazz;
		try {
			clazz = getClassLoader().loadClass(fullClassName);
			classSet.add(clazz);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
		} else {
			//不以class结尾的：递归调用addclass ;包名为package+"."+string;包路径packagepath+'/'+file
			String subPackageName=packageName+"."+fileName;
			String subPackagePath=packagePath+"/"+fileName;
			addClass(classSet, subPackagePath, subPackageName);
		}
		
	}
}

}
