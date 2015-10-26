package com;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
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

		Folder folder = cmisClient.createFolder(session);
		Document document = cmisClient
				.createDocumentFromFileWithCustomType(session);
		System.out.println(folder.getId());
		System.out.println(document.getId());
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
}
