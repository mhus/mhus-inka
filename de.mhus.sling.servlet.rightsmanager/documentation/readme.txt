Deploy the Service to allow accessing and manipulating rights via http requests. The default functionality in sling is not included in the CQ server. 

Usage:

Use the extension '.acl' on a "cq:Page", "sling:Folder", "sling:OrderedFolder", "nt:folder" to access the RightsManager. You will see the current rights for this resource and a list of sub resources and a input form where you can insert commands to change the rights.

The commands are a list of properties send via POST to the RightsManager. You can do this also via curl or wget.

List of form sling selectors:

quiet: The Rights manager will not return any output
json: The RightsManager will output json response

List of POST form properties:

create: A value of '1' will automatically create used groups if they not exists already

act: The action command section is a list of properties (like java properties files) to manipulate rights on the current resource.

Action Commands:

act=clr: General actions, the current action is 'clr'. This will delete all rights definition at the current resource before executing other commands.

All other commands need a counting number starting at zero.

pid_[nr]=[principal id]: Define the principal
[nr]_restrictions=[restrictions]: Sets the rep:glob for this acl to the [restrictions] value
[nr]_[privilege]=[0|1]: Sets the given privilege to 1=allow or 0=deny for this acl.

Build in privileges:

all: Privilege.JCR_ALL
read: Privilege.JCR_READ;
rep_write: rep:write
write: Privilege.JCR_WRITE
replicate: crx:replicate
versionManagement: jcr:versionManagement
modifyAccessControl: jcr:modifyAccessControl
readAccessControl: jcr:readAccessControl
lockManagement: jcr:lockManagement
removeNode: jcr:removeNode
delete: jcr:removeNode
nodeTypeManagement: jcr:nodeTypeManagement
addChildNodes: jcr:addChildNodes
removeChildNodes: jcr:removeChildNodes

