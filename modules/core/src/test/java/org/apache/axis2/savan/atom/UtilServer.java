package org.apache.axis2.savan.atom;

import java.io.File;
import java.io.FilenameFilter;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Flow;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.http.SimpleHTTPServer;

/**
 * 
 */
public class UtilServer {
	private static int count = 0;

	private static SimpleHTTPServer receiver;

	public static final int TESTING_PORT = 5555;

	public static synchronized void deployService(AxisService service)
			throws AxisFault {
		receiver.getConfigurationContext().getAxisConfiguration().addService(
				service);
	}

	public static synchronized void unDeployService(QName service)
			throws AxisFault {
		receiver.getConfigurationContext().getAxisConfiguration()
				.removeService(service.getLocalPart());
	}

	public static synchronized void unDeployClientService() throws AxisFault {
		if (receiver.getConfigurationContext().getAxisConfiguration() != null) {
			receiver.getConfigurationContext().getAxisConfiguration()
					.removeService("AnonymousService");
		}
	}

	public static synchronized void start() throws Exception {
		start(org.apache.axis2.Constants.TESTING_REPOSITORY);
	}

	public static synchronized void start(String repository) throws Exception {
		if (count == 0) {
			ConfigurationContext er = getNewConfigurationContext(repository);
			// er.getAxisConfiguration().addModule(new DeploymentEngine().)

			receiver = new SimpleHTTPServer(er, TESTING_PORT);

			receiver.start();
			System.out
					.print("Server started on port " + TESTING_PORT + ".....");

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				throw new AxisFault("Thread interupted", e1);
			}

		}
		count++;
	}

	

	public static ConfigurationContext getNewConfigurationContext(
			String repository) throws Exception {
		File file = new File(repository);
		if (!file.exists()) {
			throw new Exception("repository directory "
					+ file.getAbsolutePath() + " does not exists");
		}
		return ConfigurationContextFactory
				.createConfigurationContextFromFileSystem(file
						.getAbsolutePath(), file.getAbsolutePath()+"/conf/axis2.xml");
	}

	public static synchronized void stop() throws AxisFault {
		if (count == 1) {
			receiver.stop();
			while (receiver.isRunning()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
			count = 0;
			System.out.print("Server stopped .....");
		} else {
			count--;
		}
		ListenerManager listenerManager = receiver.getConfigurationContext()
				.getListenerManager();
		if (listenerManager != null) {
			listenerManager.stop();
		}
	}

	public static ConfigurationContext getConfigurationContext() {
		return receiver.getConfigurationContext();
	}

	static class AddressingFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.startsWith("addressing") && name.endsWith(".mar");
		}
	}

	private static File getAddressingMARFile() {
		File dir = new File(org.apache.axis2.Constants.TESTING_REPOSITORY);
		File[] files = dir.listFiles(new AddressingFilter());
		TestCase.assertTrue(files.length == 1);
		File file = files[0];
		TestCase.assertTrue(file.exists());
		return file;
	}

	public static ServiceContext createAdressedEnabledClientSide(
			AxisService service) throws AxisFault {
		File file = getAddressingMARFile();
		TestCase.assertTrue(file.exists());

		ConfigurationContext configContext = ConfigurationContextFactory
				.createConfigurationContextFromFileSystem(
						"target/test-resources/integrationRepo", null);
		AxisModule axisModule = DeploymentEngine.buildModule(file,
				configContext.getAxisConfiguration());
		configContext.getAxisConfiguration().addModule(axisModule);

		configContext.getAxisConfiguration().addService(service);

		return new ServiceGroupContext(configContext,
				(AxisServiceGroup) service.getParent())
				.getServiceContext(service);
	}

	public static ConfigurationContext createClientConfigurationContext()
			throws AxisFault {
		File file = getAddressingMARFile();
		TestCase.assertTrue(file.exists());

		ConfigurationContext configContext = ConfigurationContextFactory
				.createConfigurationContextFromFileSystem(
						"target/test-resources/integrationRepo", null);
		AxisModule axisModule = DeploymentEngine.buildModule(file,
				configContext.getAxisConfiguration());
		configContext.getAxisConfiguration().addModule(axisModule);
		configContext.getAxisConfiguration().engageModule(
				new QName("addressing"));
		return configContext;
	}

	public static ServiceContext createAdressedEnabledClientSide(
			AxisService service, String clientHome) throws AxisFault {
		File file = getAddressingMARFile();
		TestCase.assertTrue(file.exists());

		ConfigurationContext configContext = ConfigurationContextFactory
				.createConfigurationContextFromFileSystem(clientHome, null);
		AxisModule axisModule = DeploymentEngine.buildModule(file,
				configContext.getAxisConfiguration());

		configContext.getAxisConfiguration().addModule(axisModule);
		// sysContext.getAxisConfiguration().engageModule(moduleDesc.getName());

		configContext.getAxisConfiguration().addService(service);

		return new ServiceGroupContext(configContext,
				(AxisServiceGroup) service.getParent())
				.getServiceContext(service);
	}

}
