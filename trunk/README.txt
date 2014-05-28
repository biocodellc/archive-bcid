To get bcid running on your own server, do the following:

1. Get an EZID account and password.  See http://nt2.net/ezid for more information

2. Database stuff
    a. Get mysql running on a server
    b. Intall the appropriate tables.  See bcidCreateTables.sql for loading tables.
    NOTE: you MUST have ark:/99999/fk4 as the first dataset!, like:
    INSERT INTO datasets (datasets_id,ezidMade,ezidRequest,internalID, resourceType, prefix, users_id) VALUES (1,0,0,uuid(),"http://www.w3.org/2000/01/rdf-schema#Resource",'ark:/99999/fk4',1);

3. Update template files:
    a. Copy bcidsettings.template to bcidsettings.props and change appropriate values


###################
#  Deploying BiSciCol applications:
###################

1. BCID is the root service and is deployed by putting it at:
/opt/mywebapps/bcid

and add the following ROOT.xml file (at conf/Catalina/localhost):
<Context
  docBase="/opt/mywebapps/bcid"
  path="/"
  reloadable="true"
/>

2. Biocode-FIMS load through interface

3. Triplifier : need script to run this


###################
#  Tomcat
###################
See the following to get setup:
http://tecadmin.net/steps-to-install-tomcat-server-on-centos-rhel/#

More information/autostart on boot, etc:
http://wiki.glitchdata.com/index.php?title=Tomcat_Installation

Getting an application to become root:
http://stackoverflow.com/questions/7276989/howto-set-the-context-path-of-a-web-application-in-tomcat-7-0


###################
#  Oauth
###################
See auth/oauth2/provider to setup a client_id/client_secret

###################
# DO NOT RECCOMEND USING Glassfish
###################
# Install Glassfish
download to /usr/local/src/
extract
/usr/local/src/glassfish3/glassfish/bin/asadmin change-admin-password
(other useful notes on glassfish=http://www.davidghedini.com/pg/entry/how_to_install_glassfish_3)

# updating glassfish
./pkg image-update

# bcid application is the context root
change contextroot of bcid Application in Glassfish console to "/"

#starting and stopping
cd {$glassfishRoot}/bin/stopserv
cd {$glassfishRoot}/bin/startserv

# change master password
./asadmin
  -> change-admin-password (admin/{no password} is default)
# enabling secure admin
./asadmin
  -> enable-secure-admin
#Specific to some early glassfish installations
  Server Stuff
enable port 80 requests to forward to 8080 for ALL deployed services on this particular VM:
-A PREROUTING -i eth0 -p tcp -m tcp --dport 80 -j DNAT --to-destination :8080
port 443 is https and usually open on various restricted subnets so use this to forward query requests to the non-standard 3030
-A PREROUTING -i eth0 -p tcp -m tcp --dport 443 -j DNAT --to-destination :3030
