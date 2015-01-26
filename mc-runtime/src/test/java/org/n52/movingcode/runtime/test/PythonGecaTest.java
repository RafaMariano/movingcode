package org.n52.movingcode.runtime.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.n52.movingcode.runtime.codepackage.MovingCodePackage;
import org.n52.movingcode.runtime.iodata.IIOParameter;
import org.n52.movingcode.runtime.iodata.MediaData;
import org.n52.movingcode.runtime.processors.AbstractProcessor;
import org.n52.movingcode.runtime.processors.ProcessorFactory;

public class PythonGecaTest extends MCRuntimeTestConfig {

	private static final String packageFileName = "src/test/resources/testpackages/py_geca.zip";
	private static final String mimeType = "application/pdf";

	@Before
	public void loadPyCopyPackage() {

		// Arrange
		File packageFile = new File(packageFileName);

		// Act
		MovingCodePackage mcPackage = new MovingCodePackage(packageFile);

		// Assert
		Assert.assertTrue(mcPackage.isValid());
		Assert.assertTrue(ProcessorFactory.getInstance().newProcessor(mcPackage) != null);
	}

	@Test
	public void executePyCopyPackage() throws IllegalArgumentException, FileNotFoundException {

		// Arrange
		File packageFile = new File(packageFileName);

		// Act
		MovingCodePackage mcPackage = new MovingCodePackage(packageFile);
		AbstractProcessor processor = ProcessorFactory.getInstance().newProcessor(mcPackage);

		// add input
		Assert.assertTrue(processor.addData(new IIOParameter.ParameterID(1), "MIP_NL__2P"));
		Assert.assertTrue(processor.addData(new IIOParameter.ParameterID(2), "GOM_NL__2P"));
		Assert.assertTrue(processor.addData(new IIOParameter.ParameterID(3), "2"));
		Assert.assertTrue(processor.addData(new IIOParameter.ParameterID(4), "1000"));
		Assert.assertTrue(processor.addData(new IIOParameter.ParameterID(5), "nearest_neighbour"));
		Assert.assertTrue(processor.addData(new IIOParameter.ParameterID(6), "satellite_b"));
		// add output declaration
		Assert.assertTrue(processor.addData(new IIOParameter.ParameterID(7), new MediaData(null, mimeType)));

		// Assert
		Assert.assertTrue(processor.isFeasible());

		try {
			processor.execute(0);
			LOGGER.info(processor.pollLastEntry());
		}
		catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
