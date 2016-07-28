package com.ebay.platform.xagent.runtime.decompiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import jd.common.loader.BaseLoader;
import jd.common.loader.LoaderManager;
import jd.common.preferences.CommonPreferences;
import jd.common.printer.text.PlainTextPrinter;
import jd.core.loader.LoaderException;
import jd.core.model.classfile.ClassFile;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.writer.ClassFileWriter;

public class DecompilerByJD {
	private static LoaderManager loaderManager = new LoaderManager();
	private static CommonPreferences preferences = new CommonPreferences(false, false, true, false, false, false);
	
	public static String decompile(byte[] buffer)
	{
		try
		{
			File tempFile = File.createTempFile("xagent",".tmp");
			FileOutputStream fos = new FileOutputStream(tempFile);
		
			fos.write(buffer);
			fos.close();
		
			tempFile.deleteOnExit();
			
			return decompile(tempFile);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public static String decompile(File file)
	{
		return decompile(file.getParent(), file.getName());
	}
	
	public static String decompile(String basePath, String classPath) 
	{
		try {
			BaseLoader loader = loaderManager.getLoader(basePath);

			ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
			PrintStream ps = new PrintStream(baos);
			PlainTextPrinter printer = new PlainTextPrinter(preferences, ps);

			ClassFile classFile = ClassFileDeserializer.Deserialize(loader,
					classPath);
			if (classFile == null) {
				throw new LoaderException("Can not deserialize '" + classPath
						+ "'.");
			}
			ReferenceMap referenceMap = new ReferenceMap();
			ClassFileAnalyzer.Analyze(referenceMap, classFile);

			ReferenceAnalyzer.Analyze(referenceMap, classFile);

			ArrayList<LayoutBlock> layoutBlockList = new ArrayList<LayoutBlock>(1024);
			int maxLineNumber = ClassFileLayouter.Layout(preferences,
					referenceMap, classFile, layoutBlockList);

			ClassFileWriter.Write(loader, printer, referenceMap, maxLineNumber,
					classFile.getMajorVersion(), classFile.getMinorVersion(),
					layoutBlockList);

			ps.close();
			
			return new String(baos.toByteArray());
		} catch (Throwable t) {
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		
		System.out.println(DecompilerByJD.decompile(new File("C:\\GitHub\\XAgent\\target\\classes\\com\\ebay\\platform\\xagent\\gui\\ClassListPanel.class")));
	}

}
