/*
 * $Header: /cvsroot/webdav-servlet/webdav-servlet/src/main/java/net/sf/webdav/IWebdavStorage.java,v 1.1 2006/01/19 16:07:06 yavarin Exp $
 * $Revision: 1.1 $
 * $Date: 2006/01/19 16:07:06 $
 *
 * ====================================================================
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.sf.webdav;

import java.io.InputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for simple implementation of any store for the WebdavServlet
 * <p>
 * based on the BasicWebdavStore from Oliver Zeigermann, that was part
 * of the Webdav Construcktion Kit from slide
 * 
 */
public interface IWebdavStorage {

    /**
     * Indicates that a new request or transaction with this store involved has
     * been started. The request will be terminated by either {@link #commit()}
     * or {@link #rollback()}. If only non-read methods have been called, the
     * request will be terminated by a {@link #commit()}. This method will be
     * called by (@link WebdavStoreAdapter} at the beginning of each request.
     * 
     * 
     * @param req
     *            the principal that started this request or <code>null</code>
     *            if there is non available
     * @param parameters
     *            Hashtable containing the parameters' names and associated
     *            values configured in the <init-param> from web.xml
     * @throws Exception
     */
    void begin(HttpServletRequest req,  Hashtable parameters)
            throws Exception;

    /**
     * Checks if authentication information passed in {@link #begin(Service, Principal, Object, LoggerFacade, Hashtable)}
     * is valid. If not throws an exception.
     * @param req 
     * 
     * @throws SecurityException if authentication is not valid
     */
    // void checkAuthentication(HttpServletRequest req) throws SecurityException;
    
    /**
     * Indicates that all changes done inside this request shall be made
     * permanent and any transactions, connections and other temporary resources
     * shall be terminated.
     * @param req 
     * 
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    void commit(HttpServletRequest req) throws Exception;

    /**
     * Indicates that all changes done inside this request shall be undone and
     * any transactions, connections and other temporary resources shall be
     * terminated.
     * @param req 
     * 
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    void rollback(HttpServletRequest req) throws Exception;

    /**
     * Checks if there is an object at the position specified by
     * <code>uri</code>.
     * @param req 
     * 
     * @param uri
     *            URI of the object to check
     * @return <code>true</code> if the object at <code>uri</code> exists
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    boolean objectExists(HttpServletRequest req, String uri) throws Exception;

    /**
     * Checks if there is an object at the position specified by
     * <code>uri</code> and if so if it is a folder.
     * @param req 
     * 
     * @param uri
     *            URI of the object to check
     * @return <code>true</code> if the object at <code>uri</code> exists
     *         and is a folder
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    boolean isFolder(HttpServletRequest req, String uri) throws Exception;

    /**
     * Checks if there is an object at the position specified by
     * <code>uri</code> and if so if it is a content resource.
     * @param req 
     * 
     * @param uri
     *            URI of the object to check
     * @return <code>true</code> if the object at <code>uri</code> exists
     *         and is a content resource
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    boolean isResource(HttpServletRequest req, String uri) throws Exception;

    /**
     * Creates a folder at the position specified by <code>folderUri</code>.
     * @param req 
     * 
     * @param folderUri
     *            URI of the folder
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    void createFolder(HttpServletRequest req, String folderUri) throws Exception;

    /**
     * Creates a content resource at the position specified by
     * <code>resourceUri</code>.
     * @param req 
     * 
     * @param resourceUri
     *            URI of the content resource
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    void createResource(HttpServletRequest req, String resourceUri) throws Exception;

    /**
     * Sets / stores the content of the resource specified by
     * <code>resourceUri</code>.
     * @param req 
     * 
     * @param resourceUri
     *            URI of the resource where the content will be stored
     * @param content
     *            input stream from which the content will be read from
     * @param contentType
     *            content type of the resource or <code>null</code> if unknown
     * @param characterEncoding
     *            character encoding of the resource or <code>null</code> if
     *            unknown or not applicable
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    void setResourceContent(HttpServletRequest req, String resourceUri, InputStream content, String contentType, String characterEncoding)
            throws Exception;

    /**
     * Gets the date of the last modiciation of the object specified by
     * <code>uri</code>.
     * @param req 
     * 
     * @param uri
     *            URI of the object, i.e. content resource or folder
     * @return date of last modification, <code>null</code> declares this
     *         value as invalid and asks the adapter to try to set it from the
     *         properties if possible
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    Date getLastModified(HttpServletRequest req, String uri) throws Exception;

    /**
     * Gets the date of the creation of the object specified by <code>uri</code>.
     * @param req 
     * 
     * @param uri
     *            URI of the object, i.e. content resource or folder
     * @return date of creation, <code>null</code> declares this value as
     *         invalid and asks the adapter to try to set it from the properties
     *         if possible
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    Date getCreationDate(HttpServletRequest req, String uri) throws Exception;

    /**
     * Gets the names of the children of the folder specified by
     * <code>folderUri</code>.
     * @param req 
     * 
     * @param folderUri
     *            URI of the folder
     * @return array containing names of the children or null if it is no folder
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    String[] getChildrenNames(HttpServletRequest req, String folderUri) throws Exception;

    /**
     * Gets the content of the resource specified by <code>resourceUri</code>.
     * @param req 
     * 
     * @param resourceUri
     *            URI of the content resource
     * @return input stream you can read the content of the resource from
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    InputStream getResourceContent(HttpServletRequest req, String resourceUri) throws Exception;

    /**
     * Gets the length of the content resource specified by
     * <code>resourceUri</code>.
     * @param req 
     * 
     * @param resourceUri
     *            URI of the content resource
     * @return length of the resource in bytes,
     *         <code>-1</code> declares this value as invalid and asks the
     *         adapter to try to set it from the properties if possible
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    long getResourceLength(HttpServletRequest req, String resourceUri) throws Exception;

    /**
     * Removes the object specified by <code>uri</code>.
     * @param req 
     * 
     * @param uri
     *            URI of the object, i.e. content resource or folder
     * @throws IOException
     * 				if something goes wrong on the store level
     */
    void removeObject(HttpServletRequest req, String uri) throws Exception;
}