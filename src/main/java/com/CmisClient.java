package com;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CmisClient {
	private static Log logger = LogFactory.getLog(CmisClient.class);
	private static Map<String, Session> connections = new ConcurrentHashMap<String, Session>();

	public static void main(String[] args) throws IOException {
		CmisClient cmisClient = new CmisClient();
		String connectionName = "martinAlf01";
		Session session = cmisClient.getSession(connectionName, "test", "test",
				"test");
		RepositoryInfo repositoryInfo = session.getRepositoryInfo();
		System.out
				.println("******************root folders***********************");
		// cmisClient.listTopFolder(session);
		System.out
				.println("******************listTopFolderWithPagingAndPropFilter***********************");
		// cmisClient.listTopFolderWithPagingAndPropFilter(session);
		System.out
				.println("******************capabilities***********************");

		// cmisClient.listRepoCapabilities(repositoryInfo);
		System.out
				.println("******************createFolder***********************");

		// Folder folder = cmisClient.createFolder(session);
		// Document document = cmisClient
		// .createDocumentFromFileWithCustomType(session);
		// System.out.println(folder.getId());
		// System.out.println(document.getId());
		System.out
				.println("******************updateFolder***********************");
		String path2Object = session.getRootFolder().getPath();
		if (!path2Object.endsWith("/")) {
			path2Object += "/";
		}
		path2Object += "new";
		Folder folderToRename = (Folder) session.getObjectByPath(path2Object);
		Folder updatedFolder = cmisClient.updateFolder(folderToRename);
		System.out.println(updatedFolder.getName());
	}

	public Session getSession(String connectionName, String username,
			String pwd, String repoName) {
		Session session = connections.get(connectionName);

		if (session == null) {
			logger.info("Not connected, creating new connection to"
					+ " Alfresco with the connection id (" + connectionName
					+ ")");
			// No connection to Alfresco available, create a new one
			SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(SessionParameter.USER, username);
			parameters.put(SessionParameter.PASSWORD, pwd);
			parameters
					.put(SessionParameter.ATOMPUB_URL,
							"http://localhost:8080/chemistry-opencmis-server-fileshare-0.13.0/atom");
			parameters.put(SessionParameter.BINDING_TYPE,
					BindingType.ATOMPUB.value());
			parameters.put(SessionParameter.COMPRESSION, "true");
			parameters.put(SessionParameter.CACHE_TTL_OBJECTS, "0");

			parameters.put(SessionParameter.REPOSITORY_ID, repoName);
			session = sessionFactory.createSession(parameters);

			// Save connection for reuse
			connections.put(connectionName, session);
		} else {
			logger.info("Already connected to Alfresco with the "
					+ "connection id (" + connectionName + ")");
		}
		return session;
	}

	public void listTopFolder(Session session) {
		Folder root = session.getRootFolder();
		ItemIterable<CmisObject> contentItems = root.getChildren();
		// CmisObject can be cast to Document, Folder, Policy, or Relationship.
		for (CmisObject contentItem : contentItems) {
			if (contentItem instanceof Document) {
				Document docMetadata = (Document) contentItem;
				ContentStream docContent = docMetadata.getContentStream();
				logger.info(docMetadata.getName() + " [size="
						+ docContent.getLength() + "][Mimetype="
						+ docContent.getMimeType() + "][type="
						+ docMetadata.getType().getDisplayName() + "]");
			} else {
				logger.info(contentItem.getName() + "[type="
						+ contentItem.getType().getDisplayName() + "]");
			}
			// listProperties(contentItem);
		}
	}

	public void listRepoCapabilities(RepositoryInfo repositoryInfo) {
		RepositoryCapabilities repoCapabilities = repositoryInfo
				.getCapabilities();
		logger.info("aclCapability = "
				+ repoCapabilities.getAclCapability().name());
		logger.info("changesCapability = "
				+ repoCapabilities.getChangesCapability().name());
		logger.info("contentStreamUpdatable = "
				+ repoCapabilities.getContentStreamUpdatesCapability().name());
		logger.info("joinCapability = "
				+ repoCapabilities.getJoinCapability().name());
		logger.info("queryCapability = "
				+ repoCapabilities.getQueryCapability().name());
		logger.info("renditionCapability = "
				+ repoCapabilities.getRenditionsCapability().name());
		logger.info("allVersionsSearchable? = "
				+ repoCapabilities.isAllVersionsSearchableSupported());
		logger.info("getDescendantSupported? = "
				+ repoCapabilities.isGetDescendantsSupported());
		logger.info("getFolderTreeSupported? = "
				+ repoCapabilities.isGetFolderTreeSupported());
		logger.info("multiFilingSupported? = "
				+ repoCapabilities.isMultifilingSupported());
		logger.info("privateWorkingCopySearchable? = "
				+ repoCapabilities.isPwcSearchableSupported());
		logger.info("pwcUpdateable? = "
				+ repoCapabilities.isPwcUpdatableSupported());
		logger.info("unfilingSupported? = "
				+ repoCapabilities.isUnfilingSupported());
		logger.info("versionSpecificFilingSupported? = "
				+ repoCapabilities.isVersionSpecificFilingSupported());
	}

	public void listTopFolderWithPagingAndPropFilter(Session session) {
		Folder root = session.getRootFolder();
		OperationContext operationContext = new OperationContextImpl();
		// you could have add filter in operationContext like:
		// Set<String> propertyFilter = new HashSet<String>();
		// propertyFilter.add(PropertyIds.CREATED_BY);
		// propertyFilter.add(PropertyIds.NAME);
		// operationContext.setFilter(propertyFilter);
		int maxItemsPerPage = 5;
		operationContext.setMaxItemsPerPage(maxItemsPerPage);
		ItemIterable<CmisObject> contentItems = root
				.getChildren(operationContext);
		long numerOfPages = Math.abs(contentItems.getTotalNumItems()
				/ maxItemsPerPage);
		int pageNumber = 1;
		boolean finishedPaging = false;
		int count = 0;
		while (!finishedPaging) {
			logger.info("Page " + pageNumber + " (" + numerOfPages + ")");
			ItemIterable<CmisObject> currentPage = contentItems.skipTo(count)
					.getPage();
			for (CmisObject contentItem : currentPage) {
				logger.info(contentItem.getName() + " [type="
						+ contentItem.getType().getDisplayName() + "]");
				count++;
			}
			pageNumber++;
			if (!currentPage.getHasMoreItems()) {
				finishedPaging = true;
			}
		}
	}

	private void listProperties(CmisObject cmisObject) {
		for (Property<?> p : cmisObject.getProperties()) {
			if (PropertyType.DATETIME == p.getType()) {
				Calendar calValue = (Calendar) p.getValue();
				logger.info(" - "
						+ p.getId()
						+ " = "
						+ (calValue != null ? new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss z").format(calValue
								.getTime()) : ""));
			} else {
				logger.info(" - " + p.getId() + " = " + p.getValue());
			}
		}
	}

	public void listTypesAndSubtypes(Session session) {
		boolean includePropertyDefinitions = false;
		List<Tree<ObjectType>> typeTrees = session.getTypeDescendants(null, -1,
				includePropertyDefinitions);
		for (Tree<ObjectType> typeTree : typeTrees) {
			logTypes(typeTree, "");
		}
	}

	private void logTypes(Tree<ObjectType> typeTree, String tab) {
		ObjectType objType = typeTree.getItem();
		String docInfo = "";
		if (objType instanceof DocumentType) {
			DocumentType docType = (DocumentType) objType;
			docInfo = "[versionable=" + docType.isVersionable() + "][content="
					+ docType.getContentStreamAllowed() + "]";
		}
		logger.info(tab + objType.getDisplayName() + " [id=" + objType.getId()
				+ "][fileable=" + objType.isFileable() + "][queryable="
				+ objType.isQueryable() + "]" + docInfo);
		for (Tree<ObjectType> subTypeTree : typeTree.getChildren()) {
			logTypes(subTypeTree, tab + " ");
		}
	}

	public Folder createFolder(Session session) {
		String folderName = "OpenCMISTest";
		Folder parentFolder = session.getRootFolder();
		// Make sure the user is allowed to create a folder
		// under the root folder
		if (parentFolder.getAllowableActions().getAllowableActions()
				.contains(Action.CAN_CREATE_FOLDER) == false) {
			throw new CmisUnauthorizedException(
					"Current user does not have permission to create a "
							+ "sub-folder in " + parentFolder.getPath());
		}
		// Check if folder already exist, if not create it
		Folder newFolder = (Folder) getObject(session, parentFolder, folderName);
		if (newFolder == null) {
			Map<String, Object> newFolderProps = new HashMap<String, Object>();
			newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			newFolderProps.put(PropertyIds.NAME, folderName);
			newFolder = parentFolder.createFolder(newFolderProps);
			logger.info("Created new folder: " + newFolder.getPath()
					+ " [creator=" + newFolder.getCreatedBy() + "][created="
					+ date2String(newFolder.getCreationDate().getTime()) + "]");
		} else {
			logger.info("Folder already exist: " + newFolder.getPath());
		}
		return newFolder;
	}

	private CmisObject getObject(Session session, Folder parentFolder,
			String objectName) {
		CmisObject object = null;
		try {
			String path2Object = parentFolder.getPath();
			if (!path2Object.endsWith("/")) {
				path2Object += "/";
			}
			path2Object += objectName;
			object = session.getObjectByPath(path2Object);
		} catch (CmisObjectNotFoundException nfe0) {
			// Nothing to do, object does not exist
		}
		return object;
	}

	private String date2String(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(date);
	}

	public Document createDocument(Session session, Folder parentFolder)
			throws IOException {
		String documentName = "OpenCMISTest.txt";
		// Make sure the user is allowed to create a document
		// in the passed in folder
		if (parentFolder.getAllowableActions().getAllowableActions()
				.contains(Action.CAN_CREATE_DOCUMENT) == false) {
			throw new CmisUnauthorizedException("Current user does not "
					+ "have permission to create a document in "
					+ parentFolder.getPath());
		}
		// Check if document already exist, if not create it
		Document newDocument = (Document) getObject(session, parentFolder,
				documentName);
		if (newDocument == null) {
			// Setup document metadata
			Map<String, Object> newDocumentProps = new HashMap<String, Object>();
			newDocumentProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			newDocumentProps.put(PropertyIds.NAME, documentName);
			// Setup document content
			String mimetype = "text/plain; charset=UTF-8";
			String documentText = "This is a test document!";
			byte[] bytes = documentText.getBytes("UTF-8");
			ByteArrayInputStream input = new ByteArrayInputStream(bytes);
			ContentStream contentStream = session.getObjectFactory()
					.createContentStream(documentName, bytes.length, mimetype,
							input);
			// Create non-versioned document object
			newDocument = parentFolder.createDocument(newDocumentProps,
					contentStream, VersioningState.NONE);

			logger.info("Created new document: " + getDocumentPath(newDocument)
					+ " [version=" + newDocument.getVersionLabel()
					+ "][creator=" + newDocument.getCreatedBy() + "][created="
					+ date2String(newDocument.getCreationDate().getTime())
					+ "]");
		} else {
			logger.info("Document already exist: "
					+ getDocumentPath(newDocument));
		}
		return newDocument;
	}

	private String getDocumentPath(Document document) {
		String path2Doc = getParentFolderPath(document);
		if (!path2Doc.endsWith("/")) {
			path2Doc += "/";
		}
		path2Doc += document.getName();
		return path2Doc;
	}

	private String getParentFolderPath(Document document) {
		Folder parentFolder = getDocumentParentFolder(document);
		return parentFolder == null ? "Un-filed" : parentFolder.getPath();
	}

	private Folder getDocumentParentFolder(Document document) {
		// Get all the parent folders (could be more than one
		// if multi-filed)
		List<Folder> parentFolders = document.getParents();
		// Grab the first parent folder
		if (parentFolders.size() > 0) {
			if (parentFolders.size() > 1) {
				logger.info("The " + document.getName()
						+ " has more than one parent folder, it is multi-filed");
			}
			return parentFolders.get(0);
		} else {
			logger.info("Document " + document.getName()
					+ " is un-filed and does not have a parent folder");
			return null;
		}
	}

	public Document createDocumentFromFileWithCustomType(Session session) {
		String documentName = "OpenCMISTest2.pdf";
		File file = new File("J:/temp/Some.pdf");
		Folder parentFolder = session.getRootFolder();
		// Check if document already exist, if not create it
		Document newDocument = (Document) getObject(session, parentFolder,
				documentName);
		if (newDocument == null) {
			// Setup document metadata
			Map<String, Object> newDocumentProps = new HashMap<String, Object>();
			newDocumentProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			newDocumentProps.put(PropertyIds.NAME, documentName);
			InputStream is = null;
			try {
				// Setup document content
				is = new FileInputStream(file);
				String mimetype = "application/pdf";
				ContentStream contentStream = session.getObjectFactory()
						.createContentStream(documentName, file.length(),
								mimetype, is);
				// Create versioned document object
				newDocument = parentFolder.createDocument(newDocumentProps,
						contentStream, VersioningState.NONE);
				logger.info("Created new document: "
						+ getDocumentPath(newDocument) + " [version="
						+ newDocument.getVersionLabel() + "][creator="
						+ newDocument.getCreatedBy() + "][created="
						+ date2String(newDocument.getCreationDate().getTime())
						+ "]");
				// Close the stream to handle any IO Exception
				is.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			logger.info("Document already exist: "
					+ getDocumentPath(newDocument));
		}
		return newDocument;
	}

	public Folder updateFolder(Folder folder) {
		String newFolderName = "OpenCM";
		Folder updatedFolder = null;
		// If we got a folder update the name of it
		if (folder != null) {
			// Make sure the user is allowed to update folder properties
			if (folder.getAllowableActions().getAllowableActions()
					.contains(Action.CAN_UPDATE_PROPERTIES) == false) {
				throw new CmisUnauthorizedException(
						"Current user does not have permission to update "
								+ "folder properties for " + folder.getPath());
			}
			// Update the folder with a new name
			String oldName = folder.getName();
			Map<String, Object> newFolderProps = new HashMap<String, Object>();
			newFolderProps.put(PropertyIds.NAME, newFolderName);
			folder.updateProperties(newFolderProps, false);

		} else {
			logger.error("Folder to update is null!");
		}
		return folder;
	}

	public Document updateDocument(Session session, Document document)
			throws IOException {
		RepositoryInfo repoInfo = session.getRepositoryInfo();
		if (!repoInfo.getCapabilities().getContentStreamUpdatesCapability()
				.equals(CapabilityContentStreamUpdates.ANYTIME)) {
			logger.warn("Updating content stream without a checkout is"
					+ " not supported by this repository [repoName="
					+ repoInfo.getProductName() + "][repoVersion="
					+ repoInfo.getProductVersion() + "]");
			return document;
		}
		// Make sure we got a document, then update it
		Document updatedDocument = null;
		if (document != null) {
			// Make sure the user is allowed to update the content
			// for this document
			if (document.getAllowableActions().getAllowableActions()
					.contains(Action.CAN_SET_CONTENT_STREAM) == false) {
				throw new CmisUnauthorizedException("Current user does not"
						+ " have permission to set/update content stream for "
						+ getDocumentPath(document));
			}
			// Setup new document content
			String newDocumentText = "This is a test document that has "
					+ "been updated with new content!";
			String mimetype = "text/plain; charset=UTF-8";
			byte[] bytes = newDocumentText.getBytes("UTF-8");
			ByteArrayInputStream input = new ByteArrayInputStream(bytes);
			ContentStream contentStream = session.getObjectFactory()
					.createContentStream(document.getName(), bytes.length,
							mimetype, input);
			boolean overwriteContent = true;
			updatedDocument = document.setContentStream(contentStream,
					overwriteContent);
			if (updatedDocument == null) {
				logger.info("No new version was created when "
						+ "content stream was updated for "
						+ getDocumentPath(document));
				updatedDocument = document;
			}
			logger.info("Updated content for document: "
					+ getDocumentPath(updatedDocument)
					+ " [version="
					+ updatedDocument.getVersionLabel()
					+ "][modifier="
					+ updatedDocument.getLastModifiedBy()
					+ "][modified="
					+ date2String(updatedDocument.getLastModificationDate()
							.getTime()) + "]");
		} else {
			logger.info("Document is null, cannot update it!");
		}
		return updatedDocument;
	}

	public void deleteDocument(Document document) {
		// If we got a document try and delete it
		if (document != null) {
			// Make sure the user is allowed to delete the document
			if (document.getAllowableActions().getAllowableActions()
					.contains(Action.CAN_DELETE_OBJECT) == false) {
				throw new CmisUnauthorizedException("Current user does "
						+ "not have permission to delete document "
						+ document.getName() + " with Object ID "
						+ document.getId());
			}
			String docPath = getDocumentPath(document);
			boolean deleteAllVersions = true;
			document.delete(deleteAllVersions);
			logger.info("Deleted document: " + docPath);
		} else {
			logger.info("Cannot delete document as it is null!");
		}
	}

	public void deleteFolder(Folder folder) {
		// If we got a folder then delete
		if (folder != null) {
			// Make sure the user is allowed to delete the folder
			if (folder.getAllowableActions().getAllowableActions()
					.contains(Action.CAN_DELETE_OBJECT) == false) {
				throw new CmisUnauthorizedException("Current user does "
						+ "not have permission to delete folder "
						+ folder.getPath());
			}
			String folderPath = folder.getPath();
			folder.delete();
			logger.info("Deleted folder: " + folderPath);
		} else {
			logger.info("Cannot delete folder that is null");
		}
	}

	public void deleteFolderTree(Session session) {
		UnfileObject unfileMode = UnfileObject.UNFILE;
		RepositoryInfo repoInfo = session.getRepositoryInfo();
		if (!repoInfo.getCapabilities().isUnfilingSupported()) {
			logger.warn("The repository does not support unfiling"
					+ " a document from a folder, documents will "
					+ "be deleted completely from all associated folders "
					+ "[repoName=" + repoInfo.getProductName()
					+ "][repoVersion=" + repoInfo.getProductVersion() + "]");
			unfileMode = UnfileObject.DELETE;
		}
		String folderName = "OpenCMISTestWithContent";
		Folder parentFolder = session.getRootFolder();
		// Check if folder exist, if not don't try and delete it
		Folder someFolder = (Folder) getObject(session, parentFolder,
				folderName);
		if (someFolder != null) {
			// Make sure the user is allowed to delete the folder
			if (someFolder.getAllowableActions().getAllowableActions()
					.contains(Action.CAN_DELETE_TREE) == false) {
				throw new CmisUnauthorizedException("Current user does"
						+ " not have permission to delete folder tree"
						+ parentFolder.getPath());
			}
			boolean deleteAllVersions = true;
			boolean continueOnFailure = true;
			List<String> failedObjectIds = someFolder.deleteTree(
					deleteAllVersions, unfileMode, continueOnFailure);
			logger.info("Deleted folder and all its content: "
					+ someFolder.getName());
			if (failedObjectIds != null && failedObjectIds.size() > 1) {
				for (String failedObjectId : failedObjectIds) {
					logger.info("Could not delete Alfresco node with "
							+ "Node Ref: " + failedObjectId);
				}
			}
		} else {
			logger.info("Did not delete folder as it does not exist: "
					+ parentFolder.getPath() + folderName);
		}
	}

	public void getContentForDocumentAndStoreInFile(Session session) {
		// This is one of the out-of-the-box email templates in Alfresco
		String documentPath = "/Data Dictionary/Email Templates/invite/invite-email.html.ftl";
		// Get the document object by path so we can
		// get to the content stream
		Document templateDocument = (Document) session
				.getObjectByPath(documentPath);
		if (templateDocument != null) {
			// Make sure the user is allowed to get the
			// content stream (bytes) for the document
			if (templateDocument.getAllowableActions().getAllowableActions()
					.contains(Action.CAN_GET_CONTENT_STREAM) == false) {
				throw new CmisUnauthorizedException(
						"Current user does not have permission to get the"
								+ " content stream for " + documentPath);
			}
			File file = null;
			InputStream input = null;
			OutputStream output = null;
			try {
				// Create the file on the local drive without any content
				file = new File(templateDocument.getName());
				if (!file.exists()) {
					file.createNewFile();
				}
				// Get the object content stream and write to
				// the new local file
				input = templateDocument.getContentStream().getStream();
				output = new FileOutputStream(file);

				IOUtils.copy(input, output);
				// Close streams and handle exceptions
				input.close();
				output.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				IOUtils.closeQuietly(output);
				IOUtils.closeQuietly(input);
			}
			logger.info("Created a new file " + file.getAbsolutePath()
					+ " with content from document: " + documentPath);
		} else {
			logger.error("Template document could not be found: "
					+ documentPath);
		}
	}

	public void copyDocument(Session session, Document document) {
		Folder parentFolder = session.getRootFolder();
		String destinationFolderName = "Guest Home";
		Folder destinationFolder = (Folder) getObject(session, parentFolder,
				destinationFolderName);
		if (destinationFolder == null) {
			logger.error("Cannot copy " + document.getName()
					+ ", could not find folder with the name "
					+ destinationFolderName + ", are you using Alfresco?");
			return;
		}
		// Check that we got the document, then copy
		if (document != null) {
			try {
				document.copy(destinationFolder);
				logger.info("Copied document " + document.getName()
						+ " from folder " + parentFolder.getPath()
						+ " to folder " + destinationFolder.getPath());
			} catch (CmisContentAlreadyExistsException e) {
				logger.error("Cannot copy document " + document.getName()
						+ ", already exist in to folder "
						+ destinationFolder.getPath());
			}
		} else {
			logger.error("Document is null, cannot copy to "
					+ destinationFolder.getPath());
		}
	}

	public void copyFolder(Folder destinationFolder, Folder toCopyFolder) {
		Map<String, Object> folderProperties = new HashMap<String, Object>();
		folderProperties.put(PropertyIds.NAME, toCopyFolder.getName());
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, toCopyFolder
				.getBaseTypeId().value());
		Folder newFolder = destinationFolder.createFolder(folderProperties);
		copyChildren(newFolder, toCopyFolder);
	}

	public void copyChildren(Folder destinationFolder, Folder toCopyFolder) {
		ItemIterable<CmisObject> immediateChildren = toCopyFolder.getChildren();
		for (CmisObject child : immediateChildren) {
			if (child instanceof Document) {
				((Document) child).copy(destinationFolder);
			} else if (child instanceof Folder) {
				copyFolder(destinationFolder, (Folder) child);
			}
		}
	}

	public void moveDocument(Session session, Document document) {
		Folder parentFolder = session.getRootFolder();
		Folder sourceFolder = getDocumentParentFolder(document);
		String destinationFolderName = "User Homes";
		Folder destinationFolder = (Folder) getObject(session, parentFolder,
				destinationFolderName);
		// Check that we got the document, then move
		if (document != null) {
			// Make sure the user is allowed to move the document
			// to a new folder
			if (document.getAllowableActions().getAllowableActions()
					.contains(Action.CAN_MOVE_OBJECT) == false) {
				throw new CmisUnauthorizedException("Current user does"
						+ " not have permission to move "
						+ getDocumentPath(document) + document.getName());
			}
			String pathBeforeMove = getDocumentPath(document);
			try {
				document.move(sourceFolder, destinationFolder);
				logger.info("Moved document " + pathBeforeMove + " to folder "
						+ destinationFolder.getPath());
			} catch (CmisRuntimeException e) {
				logger.error("Cannot move document to folder "
						+ destinationFolder.getPath() + ": " + e.getMessage());
			}
		} else {
			logger.error("Document is null, cannot move!");
		}
	}
}
