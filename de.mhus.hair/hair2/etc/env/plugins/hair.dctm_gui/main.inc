<root sort="2">

    <start class="de.mhu.hair.plugin.bof.BofLoaderPlugin">
      <config>
     </config>
    </start>

    <start class="de.mhu.hair.plugin.ui.OutputPlugin">
        <config icon="hair:/out.gif" 
        		pos ="log" 
        		title="I/O" 
        		closable="0" 
        		persistent="c_io"
        		/>
    </start>
    
    <start class="de.mhu.hair.plugin.log4j.Plugin">
    	<config properties="log4j.properties"/>
    </start>
    
    <start class="de.mhu.hair.plugin.dctm.ApiTypes"/>

    <start class="de.mhu.hair.plugin.ui.MenuStructurePlugin">
      <config>
        <menu location="File" title="File"/>
        <menu location="Edit" title="Edit"/>        
        <menu location="View" title="View"/>
        <menu location="Extra" title="Extra"/>
        <menu location="Scripts" title="Scripts"/>
        <includeconfigs name="menu_structure" />

        <special type="window_manager" location="Windows"/>
        <special type="help" location="Help"/>
        
      </config>
    </start>

	<start class="de.mhu.hair.plugin.ui.ClipboardPlugin">
      <config>
        <item action="select" title="Select" location="Edit/select"/>
        <item action="copy" title="Copy" location="Edit/copy"/>
        <item action="move" title="Move" location="Edit/move"/>
        <item action="link" title="Link" location="Edit/link"/>
        <item action="print" title="Print Selection" location="Edit/print"/>
        <item action="clear" title="Clear Selection" location="Edit/clear"/>
        <item action="unlink" title="Unlink" location="Edit/unlink"/>
        <item action="unicate" title="Unlink Others" location="Edit/unicate"/>
        
      </config>
    </start>
      
    <start class="de.mhu.hair.tools.ActionsPlugin">
        <config>
        	
            <action id="dql_editor" class="de.mhu.hair.tools.actions.DqlEditor">
                <config pos="*" title="DQL" closable="1" close.operation="dispose"
                size="70%x70%" file="DCTM.pdql">
                </config>
            </action>
            <action id="api_editor" class="de.mhu.hair.tools.actions.ApiEditor">
                <config pos="*" title="API" closable="1" close.operation="dispose"
                size="70%x70%" file="DCTM.papi">
                </config>
            </action>
              
            <action id="types_tree" class="de.mhu.hair.tools.actions.TypesTree">
                <config size="70%x70%" pos="*" title="Types" closable="1" close.operation="dispose"/>
            </action>
                                    
            <action id="dbor_print_info" class="de.mhu.hair.tools.actions.PrintDborInfoAction" popup="1"/>
            
            <includexml path="res:/de/mhu/hair/tools/operations/actions.xml"/>
             
        	<includeconfigs name="action" />
            
            <menu title="Object" 			action="create_object"		location="File/Create"/>
            <menu title="Folder" 			action="create_folder" 		location="File/Create"/>
            <menu title="Cabinet" 			action="create_cabinet" 	location="File/Create"/>
            <menu title="All" 				action="publish_all" 		location="File/Publish"/>
            <menu title="Single" 			action="publish_single" 	location="File/Publish"/>
            <menu title="Edit" 				action="edit_content" 		location="File"/>
            <menu title="View" 				action="view_content" 		location="File"/>
            <menu title="Check Out" 		action="check_out" 			location="File"/>
            <menu title="Cancel Check Out" 	action="cancel_check_out" 	location="File"/>
            <menu title="Import" 			action="import_file" 		location="File"/>
            <menu title="Export" 			action="export_file" 		location="File"/>
            <menu title="Delete" 			action="delete" 			location="File"/>
            
            <menu title="DQL Editor" 		action="dql_editor" 		location="Extra"/>
            <menu title="API Editor" 		action="api_editor" 		location="Extra"/>
            <menu title="Types Tree" 		action="types_tree" 		location="Extra"/>
                                           
			<includeconfigs name="action_menu" />
			
            <button title="API" action="api_editor"/>
            <button title="DQL" action="dql_editor"/>
            
            <includeconfigs name="action_button" />
                             
            <popup action="view_content" location="Content"/>
            <popup action="edit_content" location="Content"/>
            <popup action="delete" 		title="Delete"	location="File"/>
            <popup action="export_file" title="Export" 	location="File"/>
            <popup title="Check Out" 		action="check_out" 			location="File"/>
            <popup title="All" 				action="publish_all" 		location="File/Publish"/>
            <popup title="Single" 			action="publish_single" 	location="File/Publish"/>
            
            <includeconfigs name="action_popup" />
            
        </config>
    </start>
        
    <start class="de.mhu.hair.plugin.ui.TreePlugin">
        <config icon="hair:/tree.gif" 
        		pos="*" 
        		title="Folders" 
        		closable="1" 
        		persistent="c_tree"
        		location="View/TreePlugin"
        		visible.default="1"
				listen="_obj.hotselect_"
        		/>
    </start>

    <start class="de.mhu.hair.plugin.ui.DocumentPlugin">
        <config icon="hair:/tree.gif" 
        		pos="*" 
        		title="Documents" 
        		closable="1" 
        		persistent="c_doc"
        		location="View/DocumentPlugin"
        		visible.default="1"
				listen="_obj.hotselect_"
        		/>
    </start>
    
    <start class="de.mhu.hair.plugin.ui.AttributeListPlugin">
        <config	icon="hair:/explorer.gif" 
        		pos="*" 
        		title="Attributes" 
        		closable="1" 
        		persistent="c_attr" 
        		listen="_obj.hotselect_"
        		location="View/AttributeListPlugin"
        		visible.default="1"
        		/>
    </start>

    <start class="de.mhu.hair.plugin.ui.TypeAttrPlugin">
        <config icon="hair:/emptypoint.gif" 
        		pos="*" 
        		title="Type" 
        		closable="1" 
        		persistent="c_typeattr" 
        		listen="_obj.hotselect_"
        		location="View/TypeAttrPlugin"
        		visible.default="1"
        		/>
    </start>
    
    <start class="de.mhu.hair.plugin.ui.RelationsPlugin">
        <config icon="hair:/hair.gif" 
        		pos="*" 
        		title="Relations" 
        		closable="1" 
        		persistent="c_relations"  
        		listen="_obj.hotselect_"
        		location="View/RelationsPlugin"
        		visible.default="0"
        		/>
        		
    </start>
    
    <start class="de.mhu.hair.plugin.ui.VersionsPlugin">
        <config	icon="hair:/hair.gif" 
        		pos="*" 
        		title="Versions" 
        		closable="1" 
        		persistent="c_versions" 
        		listen="_obj.hotselect_"
        		location="View/VersionsPlugin"
        		visible.default="0"
        		/>
    </start>
    
    <!-- start class="de.mhu.hair.plugin.ui.DummyPlugin">
        <config pos="top" title="Test" text="Das ist ein test"/>
    </start -->
    
    <start class="de.mhu.hair.plugin.ui.StatusPlugin">
      <config listen="_obj.hotselect_"/>
    </start>

    <start class="de.mhu.hair.plugin.ui.SysStatusPlugin"/>
    
    <start class="de.mhu.hair.tools.ObjectFromIdPlugin"/>

    <start class="de.mhu.hair.tools.FactoryPlugin">
        <config>
            <listener listen="_obj.select_">
                <start class="de.mhu.hair.plugin.ui.InnerFramePlugin">
                    <config pos="*" title="$objname$ [$objpath$]" size="500x300" closable="1" close.operation="dispose">
                        <tabs>
                            <position name="Attributes" pos="bottom"/>
	                        <position name="Relations" pos="top"/>
	                        <position name="Types" pos="types"/>
	                   </tabs>
                    </config>
                </start>
				             
                <start class="de.mhu.hair.plugin.ui.RelationsPlugin">
                    <config pos="top" listen="_obj.last_"/>
                </start>
                <start class="de.mhu.hair.plugin.ui.AttributeListPlugin">
                    <config pos="bottom" listen="_obj.last_"/>
                </start>
                <start class="de.mhu.hair.plugin.ui.TypeAttrPlugin">
                    <config icon="hair:/emptypoint.gif" pos="types" title="Types" listen="_obj.last_"/>
                </start>
            </listener>
            
            <listener listen="_obj.worker.factory_" title="New Window">
            	<start class="de.mhu.hair.plugin.ui.InnerFramePlugin">
                    <config pos="*" title="$objname$ [$objpath$]" size="80%x80%" closable="1" close.operation="dispose">
                        <tabs>
                            <position name="Attributes" pos="bottom"/>
	                        <position name="Relations" pos="top"/>
	                        <position name="Types" pos="types"/>
	                   </tabs>
                    </config>
                </start>

                <start class="de.mhu.hair.plugin.ui.RelationsPlugin">
                    <config pos="top" listen="_obj.last_"/>
                </start>
                <start class="de.mhu.hair.plugin.ui.AttributeListPlugin">
                    <config pos="bottom" listen="_obj.last_"/>
                </start>
                <start class="de.mhu.hair.plugin.ui.TypeAttrPlugin">
                    <config icon="hair:/emptypoint.gif" pos="types" title="Types" listen="_obj.last_"/>
                </start>
            </listener>
            
            <!-- listener listen="_obj.worker.factory_" title="New Tree">
            	<start class="de.mhu.hair.plugin.ui.InnerFramePlugin">
                    <config pos="*" title="Object [$objpath$]" size="500x300" closable="1" close.operation="dispose">
                        <position pos="main" min="200x200"/>
                    </config>
                </start>
                <start class="de.mhu.hair.plugin.ui.TreePlugin">
                    <config pos="main" listen="_obj.last_"/>
                </start>
            </listener -->
            
            <listener listen="_obj.worker.factory_" title="Dump">
            	<start class="de.mhu.hair.plugin.ui.InnerFramePlugin">
                    <config pos="*" title="Dump $objname$ [$objpath$]" size="80%x80%" closable="1" close.operation="dispose">
                        <position pos="main" min="200x200"/>
                    </config>
                </start>
                <start class="de.mhu.hair.tools.DumpPlugin">
                    <config pos="main" listen="_obj.last_"/>
                </start>
            </listener>
            <listener listen="_obj.worker.factory_" title="Show Class Informations">
            	<start class="de.mhu.hair.tools.ClassInformationsPlugin"/>
            </listener>
            
        </config>
    </start>

	<start class="de.mhu.hair.tools.HotSelectDirectPlugin"/>

	<start class="de.mhu.hair.plugin.dctm.DctmPingPlugin">
        <config interval="1200" dql="select name from dm_type where name='dm_document'" />
    </start>

    <start class="de.mhu.hair.plugin.ui.OptionsMenuPlugin">
      <config 
      		title="Options"
      		location="options"
      	/>
    </start>

   <start class="de.mhu.hair.plugin.views.DmCabinetView"/>

   <start includeconfigs="plugins" />
  
</root>