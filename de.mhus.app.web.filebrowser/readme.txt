
Install the war file in you tomcat. By default it will create a space on the filepath to your root directory. 
You should change it by creating a filebrowser.properties in your tomcat home.

Name spaces and add the names to the comma separated list in property:

spaces=

Now you can define the spaces. Use the unique space name append a dot and the property name to set a value.

The must have value is .title. It define the web title of the space. Optional you can define a .type the default
space type is 'fs' and refers to an implementation to show file system content.

Example:

spaces=foohome,share

foohome.title=Foo's Home Folder
foohome.path=/home/foo

share.title=Share
share.path=/tmp/share

The default theme is a bootstrap theme. You can change it if you set the 'content' parameter. Use a @ at the beginning
to define a default theme inside the war file.

like:

content=@simple

Or define a path to your own theme. For this unzip the war file and copy the folder 'simple' somewhere and manipulate
the property

content=/somewhere/simple

