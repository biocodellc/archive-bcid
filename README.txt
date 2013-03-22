To get bcid running on your own server, do the following:

1. Get an EZID account and password.  See http://nt2.net/ezid for more information
2. Database stuff
    a. Get mysql running on a server somewhere
    b. Intall the appropriate tables.  See bcidCreateTables.sql for loading tables.
    NOTE: you MUST have ark:/99999/fk4 as the first dataset!, like:
    INSERT INTO datasets (datasets_id,ezidMade,ezidRequest,internalID, resourceType, prefix, users_id) VALUES (1,0,0,uuid(),"http://www.w3.org/2000/01/rdf-schema#Resource",'ark:/99999/fk4',1);

3. Update template files:
    a. Copy bcidsettings.template to bcidsettings.props and change appropriate values
    b. Copy /web/WEB-INF/database-template.xml to /web/WEB-INF/database.xml