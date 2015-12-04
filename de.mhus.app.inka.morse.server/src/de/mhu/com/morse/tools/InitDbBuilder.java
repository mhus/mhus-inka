package de.mhu.com.morse.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.lib.log.SwingAppender;
import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.InitialChannelDriver;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.obj.ITable;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.types.Types;
import de.mhu.com.morse.utils.DummyConnection;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.lib.plugin.AfPluginRoot;
import de.mhu.lib.plugin.utils.ALLogger;
import de.mhu.lib.plugin.utils.XmlConfig;
import de.mhu.lib.swing.table.OpenListTableModel;
import de.mhu.lib.utils.Properties;

public class InitDbBuilder {

	private static InitialChannelDriver initDb;
	private static AL log = new AL( InitDbBuilder.class );
	private static File store;
	private static File diffStore;

	static public void main( String[] args ) throws Exception {
		
		ConfigManager.initialize();
		
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		chooser.setCurrentDirectory( new File( "." ) );
		chooser.setSelectedFile( new File( ConfigManager.getConfig( "server" ).getProperty( "init.schema.def.path" )));
		chooser.setDialogTitle( "Choose Store" );
		int ret = chooser.showOpenDialog( null );
		if ( ret != JFileChooser.APPROVE_OPTION ) return;
		store = chooser.getSelectedFile();
		
		chooser.setDialogTitle( "Choos a diff store or cancel" );
		chooser.setDialogTitle( "Choose Store" );
		ret = chooser.showOpenDialog( null );
		if ( ret == JFileChooser.APPROVE_OPTION ) {
			diffStore = chooser.getSelectedFile();
		}
		if ( store.equals( diffStore ) )
			diffStore = null;
		
		ConfigManager.getConfig( "server" ).setProperty( "init.schema.def.class", "de.mhu.com.morse.channel.init.FileLoader" );
		ConfigManager.getConfig( "server" ).setProperty( "init.schema.def.path", store.getAbsolutePath() );
		ConfigManager.getConfig( "server" ).setProperty( "init.parsevalues", "0" );
		
		SwingAppender sa = new SwingAppender();
		AL.eventLog().register( sa );
		
		ConfigManager.getConfig( "server" ).setProperty( "core.module.path", "." );
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.getContentPane().add( sa );
		frame.setTitle( "MORSE LOG" );
		frame.setLocation( 200, 100 );
		frame.setSize( 1000, 800 );
		frame.show();

		AfPluginRoot root = new AfPluginRoot();
		root.enable();
		
		ALLogger logger = new ALLogger();
		XmlConfig config = new XmlConfig( "config.xml" );
		root.addPlugin( logger, "log" );
		root.refreshTools();
		root.addPlugin( config, "config" );
		root.refreshTools();
		
		root.enablePlugin( "config" );
		root.enablePlugin( "log" );

		// 1. Create init db to provide base config
		initDb = new InitialChannelDriver();
		root.addPlugin( initDb, "initDb" );
		root.enablePlugin( "initDb" );
		
		// 2. Create type provider to provide types from init db
		Types types = new Types();
		root.addPlugin( types, "types" );
		
		
		
		frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setTitle( "MORSE INIT DB BUILDER" );
		frame.setLocation( 400, 100 );
		frame.setSize( 1000, 800 );
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.setTabPlacement( JTabbedPane.LEFT );
		TreeMap<String, IType> sort = new TreeMap<String, IType>();
		for ( Iterator<IType> i = types.getTypes(); i.hasNext(); ) {
			IType t = i.next();
			sort.put(t.getName(), t );
		}
		
		for ( Iterator<IType> i = sort.values().iterator(); i.hasNext(); ) {
			TypeEditor typeEditor = new TypeEditor( i.next() );
			tabs.addTab( typeEditor.getType().getName(), typeEditor );
		}
		frame.getContentPane().add( tabs );
		
		frame.show();
		
		
		
	}
	
	
	public static class TypeEditor extends JPanel {

		private IType type;
		private DefaultListModel objModel;
		private JList objList;
		private JPanel attr;
		private Hashtable<String, Object> selectedId;
		private JButton bSave;
		private JButton bDelete;
		private JButton bFolderCalc;
		private JButton bIndex;

		public TypeEditor(IType pType) {
			type = pType;
			
			setLayout( new BorderLayout() );
			
			JSplitPane split = new JSplitPane();
			objModel = new DefaultListModel();
			objList = new JList( objModel );
			JScrollPane scroller = new JScrollPane( objList );
			split.setLeftComponent( scroller );
			attr = new JPanel();
			attr.setLayout( new BorderLayout() );
			scroller = new JScrollPane( attr );
			split.setRightComponent( scroller );
			
			add( split );
			
			objList.addListSelectionListener( new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					String id = (String)objList.getSelectedValue();
					if ( id == null ) {
						cleanAttr();
						return;
					}
					showAttr( id );
				}
				
			});
			
			JButton bNew = new JButton( " New " );
			bNew.addActionListener( new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					actionNew();
				}
				
			});
			
			bSave = new JButton( " Save " );
			bSave.addActionListener( new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					actionSave();
				}
				
			});
			
			bDelete = new JButton( " Delete " );
			bDelete.addActionListener( new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					actionDelete();
				}
				
			});
				
			bIndex = new JButton( " Indexing " );
			bIndex.addActionListener( new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					actionIndexing();
				}
				
			});
			
			JToolBar tb = new JToolBar();
			tb.add( bNew );
			tb.add( bSave );
			tb.add( bDelete );
			tb.add( bIndex );
			
			if ( getType().isInstanceOf( IType.TYPE_MC_OBJECT ) ) {
				bFolderCalc = new JButton( " Folders " );
				bFolderCalc.addActionListener( new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						actionFolderCalc();
					}
					
				});
				tb.add( bFolderCalc );
			}

				
			add( tb, BorderLayout.NORTH );
			
			fillObjList();
		}

		protected void actionIndexing() {
			
			try {
				File[] list = store.listFiles();
				FileOutputStream fos = new FileOutputStream( new File( store, "index" ) );
				PrintStream ps = new PrintStream( fos );
				for ( int i = 0; i < list.length; i++ ) {
					if ( list[i].isDirectory() && !list[i].getName().startsWith( "." ) )
						ps.println( list[i].getName() );
				}
				ps.flush();
				ps.close();

				for ( int i = 0; i < list.length; i++ ) {
					if ( list[i].isDirectory() && !list[i].getName().startsWith( "." ) ) {
						File[] list2 = list[i].listFiles();
						fos = new FileOutputStream( new File( list[i], "index" ) );
						ps = new PrintStream( fos );
						for ( int j = 0; j < list2.length; j++ ) {
							if ( list2[j].isFile() && list2[j].getName().endsWith( ".txt" ) )
								ps.println( list2[j].getName() );
						}
						ps.flush();
						ps.close();
					}
				}
				
			} catch ( Exception e ) {
				log.error( e );
				JOptionPane.showMessageDialog( this, e.toString() );
			}
		}
		
		protected void actionFolderCalc() {
			
			String id = (String)objList.getSelectedValue();
			if ( id == null ) return;
			
			try {
				OpenListTableModel folders = (OpenListTableModel)selectedId.get( "folder" );
				folders.clean();
				
				String parent = ((JTextField)selectedId.get( "parent" )).getText();
				if ( ObjectUtil.validateId( parent ) ) {
					IQueryResult res = initDb.createChannel( null ).fetch( parent, null, false );
					res.next();
					ITableRead parentFolder = res.getTable( "folder" );
					while ( parentFolder.next() ) {
						folders.addItem( new String[] { 
								parentFolder.getString( "path" ) + res.getString( "name" ) + '/', 
								parentFolder.getBoolean( "link" ) ? "1" : "0" 
								} );
					}
					parentFolder.close();
					res.close();
				} else {
					folders.addItem( new String[] { "/", "0" } );
				}
				
				OpenListTableModel links = (OpenListTableModel)selectedId.get( "links" );
				for ( int i = 0; i < links.getRows(); i++ ) {
					IQueryResult res = initDb.createChannel( null ).fetch( (String)links.getValueAt( i, 1 ), null, false );
					res.next();
					ITableRead parentFolder = res.getTable( "folder" );
					while ( parentFolder.next() ) {
						folders.addItem( new String[] { 
								parentFolder.getString( "path" ) + res.getString( "name" ) + '/', 
								"1" 
								} );
					}
					parentFolder.close();
					res.close();
				}
			} catch ( Exception e ) {
				log.error( e );
				JOptionPane.showMessageDialog( this, e.toString() );
			}
		}

		protected void actionDelete() {
			
			String id = (String)objList.getSelectedValue();
			if ( id == null ) return;
			
			JOptionPane.showConfirmDialog( this, "Really delete " + id );
			
			IType t = getType();
			while ( t != null ) {
				delete( t.getName(), id );
				t = t.getSuperType();
			}
			
			fillObjList();
			cleanAttr();
		}

		protected void actionSave() {
			if ( selectedId == null )
				return;
			
			String id = (String)selectedId.get( IAttribute.M_ID );
			
			Hashtable<String, Properties> props = new Hashtable<String, Properties>();
			IType t = getType();
			while ( t != null ) {
				Properties p = new Properties();
				p.setProperty( IAttribute.M_ID.toUpperCase(), id );
				props.put( t.getName(), p );
				t = t.getSuperType();
			}
			
			for ( Iterator<IAttribute> i = getType().getAttributes(); i.hasNext(); ) {
				IAttribute a = i.next();
				if ( ! a.getName().equals( IAttribute.M_ID ) ) {
					for ( Iterator<String> l = props.keySet().iterator(); l.hasNext(); ) {
						if ( a.isTable() ) {
							OpenListTableModel oltm = (OpenListTableModel)selectedId.get( a.getName() );
							String ln = l.next();
							props.get( ln ).setProperty( a.getName().toUpperCase() + ".SIZE", "" + oltm.getRows() );
							for ( int j = 0; j < oltm.getRows(); j++ ) {
								int cnt = 1;
								for ( Iterator<IAttribute> k = a.getAttributes(); k.hasNext(); ) {
									String val = (String)oltm.getValueAt( j, cnt );
									if ( val == null ) val = "";
									props.get( ln ).setProperty( a.getName().toUpperCase() + '.' + j + '.' + k.next().getName().toUpperCase(), val );
									cnt++;
								}
							}
						} else {
							props.get( l.next() ).setProperty( a.getName().toUpperCase(), ((JTextField)selectedId.get( a.getName() )).getText() );
						}
					}
				}
			}
			
			props.get( IType.TYPE_OBJECT ).setProperty( IAttribute.M_TYPE.toUpperCase(), getType().getName() );
			
			for ( Iterator<String> i = props.keySet().iterator(); i.hasNext(); ) {
				props.get( i.next() ).setProperty( "_DESTINATION", ((JTextField)selectedId.get( "_destination" )).getText() );
			}
			
			for ( Iterator<String> i = props.keySet().iterator(); i.hasNext(); ) {
				String tName = i.next();
				Properties p = props.get( tName );
				store( tName, id, p );
			}
			
			initDb.reloadTypes();
			
		}

		protected void actionNew() {
			String name = JOptionPane.showInputDialog( this, "Name:" );
			if ( name == null ) return;
			
			String id = '_' + getType().getName() + '_' + name;
			if ( id.length() > 32 )
				id = id.substring( 0, 32 );
			while ( id.length() < 32 )
				id = id + '_';
			
			if ( objModel.contains( id ) ) {
				JOptionPane.showMessageDialog( this, "ID already exists" );
				return;
			}
			
			Hashtable<String, Properties> props = new Hashtable<String, Properties>();
			IType t = getType();
			while ( t != null ) {
				Properties p = new Properties();
				p.setProperty( IAttribute.M_ID.toUpperCase(), id );
				props.put( t.getName(), p );
				t = t.getSuperType();
			}
			
			for ( Iterator<IAttribute> i = getType().getAttributes(); i.hasNext(); ) {
				IAttribute a = i.next();
				if ( ! a.getName().equals( IAttribute.M_ID ) ) {
					log.info( "SET ATTR: " + a.getName() + ' ' + a.getDefaultValue() );
					for ( Iterator<String> j = props.keySet().iterator(); j.hasNext(); ) {
						Properties p = props.get( j.next() );
						if ( a.isTable() ) {
							p.setProperty( a.getName().toUpperCase() + ".SIZE", "0" );
						} else
						if ( a.getName().equals( "name" ) ) {
							p.setProperty( a.getName().toUpperCase(), name );
						} else {
							p.setProperty( a.getName().toUpperCase(), a.getDefaultValue() );
						}
					}
				}
			}
						
			// props.get( IType.TYPE_OBJECT ).setProperty( IAttribute.M_TYPE.toUpperCase(), getType().getName() );
			
			// overwrite defaults
			for ( Iterator<String> i = props.keySet().iterator(); i.hasNext(); ) {
				Properties p = props.get( i.next() );
				p.setProperty( "_DESTINATION", "sys" );
				p.setProperty( IAttribute.M_TYPE.toUpperCase(), getType().getName() );
				p.setProperty( IAttribute.M_ACL.toUpperCase(), "administrator" );
			}
			
			for ( Iterator<String> i = props.keySet().iterator(); i.hasNext(); ) {
				String tName = i.next();
				Properties p = props.get( tName );
				store( tName, id, p );
			}

			if ( getType().getName().equals( IType.TYPE_TYPE ) ) {
				storeCreateDir( name );
			}
			
			initDb.reloadTypes();
			fillObjList();
			showAttr( id );
		}

		protected void showAttr(String id) {
			cleanAttr();

			try {
				IQueryResult res = initDb.createChannel( null ).fetch( id, null, false );
				res.next();
				
				Hashtable<String,Object> out = new Hashtable<String, Object>();
				out.put( IAttribute.M_ID, id );
				
				boolean ro = ! res.getString( IAttribute.M_TYPE ).equals( getType().getName() );
				
				bSave.setEnabled( !ro );
				bDelete.setEnabled( !ro );
				if ( bFolderCalc != null ) bFolderCalc.setEnabled( !ro );
				
				JPanel p = new JPanel();
				p.setLayout( new BoxLayout( p, BoxLayout.Y_AXIS ) );
				JTextField tId = new JTextField( id );
				tId.setEditable( false );
				p.add( tId );
				
				{
					p.add( new JLabel( "DB Destination:" ) );
					JTextField tf = new JTextField();
					if ( ro ) tf.setEditable( false );
					tf.setText( res.getString( "_DESTINATION" ) );
					out.put( "_destination", tf );
					p.add( tf );
				}
				
				for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
					IAttribute attr = i.next();
					if ( !attr.getName().equals( IAttribute.M_ID ) ) {
						
						p.add( new JLabel( attr.getName() + " (" + IAttribute.TITLES_AT[ attr.getType() ] + ") " + attr.getExtraValue()  ) );
						if ( attr.isTable() ) {
							OpenListTableModel oltm = new OpenListTableModel();
							JTable table = new JTable( oltm );
							if ( ro ) table.setEnabled( false );
							oltm.setTable( table );
							JScrollPane scroller = new JScrollPane( table );
							scroller.setPreferredSize( new Dimension( 100, 100 ) );
							p.add( scroller );
							LinkedList<String> titles = new LinkedList<String>();
							LinkedList<String> defaults = new LinkedList<String>();
							for ( Iterator<IAttribute> j = attr.getAttributes(); j.hasNext(); ) {
								IAttribute a = j.next();
								titles.add( a.getName() + " (" + IAttribute.TITLES_AT[ a.getType() ] + ") " + a.getExtraValue() );
								defaults.add( a.getDefaultValue() );
							}
							oltm.setCols( titles.size() );
							oltm.setDefaults( defaults.toArray() );
							oltm.setRules( new int[ titles.size() ] );
							oltm.setTitles( titles.toArray() );
							
							ITableRead resT = res.getTable( attr.getName() );
							while ( resT.next() ) {
								titles = new LinkedList<String>();
								for ( Iterator<IAttribute> j = attr.getAttributes(); j.hasNext(); )
									titles.add( resT.getString( j.next().getName() ) );
								oltm.addItem( titles.toArray() );
							}
							
							out.put( attr.getName(), oltm );
							
						} else {
							JTextField tf = new JTextField();
							if ( ro || attr.getName().equals( IAttribute.M_TYPE ) ) tf.setEditable( false );
							tf.setText( res.getString( attr.getName() ) );
							out.put( attr.getName(), tf );
							p.add( tf );
						}
						
					}
				}
			
				attr.add( p, BorderLayout.NORTH );
				attr.revalidate();
				attr.repaint();
				selectedId = out;
				
			} catch ( Exception e ) {
				log.error( e );
			}
			
		}

		protected void cleanAttr() {
			attr.removeAll();
			selectedId = null;
			attr.revalidate();
			attr.repaint();
		}

		private void fillObjList() {
			objModel.clear();
			try {
				IChannelServer channel = initDb.createChannel( null );
				Query q = new Query( new DummyConnection( channel ), "SELECT * FROM " + type.getName() );
				IQueryResult res = q.execute();
				while ( res.next() ) {
					objModel.addElement( res.getString( IAttribute.M_ID ) );
				}
			} catch ( Exception e ) {
				log.error( e );
			}
		}

		public IType getType() {
			return type;
		}
		
	}

	public static void storeCreateDir( String typeName ) {
		File typeStore = new File( store, typeName );
		if ( ! typeStore.exists() )
			typeStore.mkdirs();
		
		if ( diffStore != null ) {
			typeStore = new File( diffStore, typeName );
			if ( ! typeStore.exists() )
				typeStore.mkdirs();
		}
		
	}
	
	public static void store( String typeName, String id, Properties p) {
		
		try {
			File typeStore = new File( store, typeName );
			if ( ! typeStore.exists() )
				typeStore.mkdirs(); // paranoia
			
			FileOutputStream fos = new FileOutputStream( new File( typeStore, id + ".txt" ) );
			p.store( fos );
			fos.close();
			
			if ( diffStore != null ) {
				typeStore = new File( diffStore, typeName );
				if ( ! typeStore.exists() )
					typeStore.mkdirs(); // paranoia
				
				fos = new FileOutputStream( new File( typeStore, id + ".txt" ) );
				p.store( fos );
				fos.close();			
			}
		} catch ( Exception e ) {
			log.error( e );
		}
	}


	public static void delete(String typeName, String id) {
		try {
			File typeStore = new File( store, typeName );
			
			new File( typeStore, id + ".txt" ).delete();
			
			if ( diffStore != null ) {
				typeStore = new File( diffStore, typeName );
				if ( ! typeStore.exists() )
					typeStore.mkdirs(); // paranoia
				
				new File( typeStore, id + ".txt" ).delete();
			}
		} catch ( Exception e ) {
			log.error( e );
		}
	}
}
