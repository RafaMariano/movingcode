/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.movingcode.runtime.coderepository;

import java.io.File;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.n52.movingcode.runtime.codepackage.MovingCodePackage;

/**
 * This class implements an {@link IMovingCodeRepository} for local zipped packages, stored
 * in a possibly nested folder structure.
 * 
 * <absPath>-<folder1>\<zip1.1>
 *          |         \<zip1.2>
 *          |         \<zip1.3>
 *          |
 *          -<folder2>\<zip2.1>
 *          |         \<zip2.2>
 *          |
 *          -<folder3>\<zip3.1>
 *          |         \<zip1.2>
 *          |         \<zip1.3>
 *          |         \<zip1.4>
 *          |         \<zip1.5>
 *          |
 *          ...
 *          |
 *          -<folderN>\<zipN.1>
 *                    \<zipN.2>
 * 
 * For any zip file found in <absPath> it will be assumed that it potentially contains a zipped
 * Code Package. Thus, if the parser encounters a zip file, it will attempt an
 * interpretation as a Code Package.
 * 
 * This Repo performs occasional checks for updated content.
 * (Interval for periodical checks is given by {@link IMovingCodeRepository#localPollingInterval})
 * 
 * @author Matthias Mueller, TU Dresden
 *
 */
public final class LocalZipPackageRepository extends AbstractRepository {

	private static final String[] ZIP_EXTENSION = {"zip"};

	private final File directory;

	private String fingerprint;

	private Timer timerDaemon;

	/**
	 * 
	 * Constructor for file system based repositories. Scans all sub-directories of a given sourceDirectory
	 * for zip-Files and attempts to interpret them as MovingCodePackages. Zipfiles that do not validate will
	 * be ignored
	 * 
	 * @param sourceDirectory {@link File} - the directory to be scanned for Moving Code Packages.
	 * 
	 */
	public LocalZipPackageRepository(final File sourceDirectory) {
		this.directory = sourceDirectory;
		// compute directory fingerprint
		fingerprint = RepositoryUtils.directoryFingerprint(directory);

		// load packages from folder
		reloadContent();

		// start timer daemon
		timerDaemon = new Timer(true);
		timerDaemon.scheduleAtFixedRate(new CheckFolder(), 0, IMovingCodeRepository.localPollingInterval);
	}

	private synchronized void reloadContent(){
		PackageInventory newInventory = new PackageInventory();
		
		// recursively obtain all zipfiles in sourceDirectory
		Collection<File> zipFiles = scanForZipFiles(directory);

		logger.info("Scanning directory: " + directory.getAbsolutePath());

		for (File currentFile : zipFiles) {
			logger.debug("Found package: " + currentFile);

			MovingCodePackage mcPackage = new MovingCodePackage(currentFile);

			// validate
			// and add to package map
			// and add current file to zipFiles map
			if (mcPackage.isValid()) {
				newInventory.add(mcPackage);
				logger.debug("Registered package: " + currentFile + "; using ID: " + mcPackage.getVersionedPackageId().toString());	
			} else {
				logger.error(currentFile.getAbsolutePath() + " is an invalid package.");
			}

		}
		
		updateInventory(newInventory);
	}

	/**
	 * Scans a directory recursively for zipFiles and adds them to the global Collection "zipFiles".
	 * 
	 * @param directory {@link File} - parent directory to scan.
	 * @return A {@link Collection} of {@link File} - the zipfiles found
	 */
	public static Collection<File> scanForZipFiles(File directory) {
		return FileUtils.listFiles(directory, ZIP_EXTENSION, true);
	}

	/**
	 * A task which re-computes the directory's fingerprint and
	 * triggers a content reload if required.
	 * 
	 * @author Matthias Mueller
	 *
	 */
	private final class CheckFolder extends TimerTask {

		@Override
		public void run() {
			String newFingerprint = RepositoryUtils.directoryFingerprint(directory);
			if (!newFingerprint.equals(fingerprint)){
				logger.info("Repository content has silently changed. Running update ...");
				// set new fingerprint
				fingerprint = newFingerprint;
				
				// ... and trigger reload
				reloadContent();
			}			
		}
	}

}
