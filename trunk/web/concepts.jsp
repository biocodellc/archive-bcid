<%@ include file="header.jsp" %>

<div id="concepts" class="section">
    <h2>Concepts</h2>

    <div class="sectioncontent">
       Following is a listing of concept types available to bcids.  We have chosen to restrict concepts to those types
       commonly in use in the Bio-collections community.  Concepts draw on Dublin Core (dcterms and dctype),
        Darwin Core (dwc), the Information Artifact Ontology (iao), the Ontology for Biomedical Investigations (OBI),
        the Environment Ontology (envo), and the RDF schema (rdfs).
        <p></p>
        <div name=resourceTypes id=resourceTypes style="overflow:auto;">Loading resourceTypes table ...</div>
    </div>
</div>

<script>
    window.onload = populateResourceTypes('resourceTypes');
</script>

<%@ include file="footer.jsp" %>
