package de.mhu.com.morse.channel.fs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.PropertyQueryDefinition;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class FileChannelDriver extends FileAbstractDriver {

}
