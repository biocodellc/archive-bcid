<%@ include file="../header.jsp" %>


<div id="uuidIDs" class="section">

    <h2>Turn Local ID's or UUIDs into Identifiers</h2>

    <div class="sectioncontent">
        Paste in your Local Identifiers or UUIDs to create an identifier for each data element.
        You may optionally supply a dataset designation to apply identifiers to.
        <li>An EZID will be created for each dataset. Individual elements will be resolved to the dataset level
            through EZID.
        </li>
        <li>A BCID will be created for each data element. Individual elements will be uniquely resolved by this
            system.
        </li>
        View <a href="http://code.google.com/p/biscicol/wiki/Identifiers">help</a> to get more information on this
        section.


        <br>

        <form id="localIDMinterForm" action="/bcid/api/bcidService" method="POST">
            <table>
                <tr>
                    <td>Concept:</td>
                    <td>
                        <select name="concept" id=concept class="">
                        </select></td>
                    <td>A concept describing the set of elements in this dataset.</td>
                </tr>
                <tr>
                    <td>Dataset:</td>
                    <td>
                        <input type=textbox name=dataset>
                    </td>
                    <td>Apply these identifiers to an existing dataset (optional. E.g. ark:/87286/A2)</td>
                </tr>
                <tr>
                    <td colspan=3><input type=checkbox name=ezidAsUUID> These are UUIDs</td>
                </tr>
                <tr>
                    <td colspan=3><textarea name="data" cols="80"
                                            rows="10">LocalIdentifier&lt;tab&gt;TargetURL</textarea></td>
                </tr>

                <tr>
                    <td colspan=3>
                        <input type=hidden name=email value=USER@EMAIL.HERE>
                        <input type="button" value="validate" onclick="validate()"/>&nbsp;&nbsp;
                        <input type="button" value="send" onclick="send('localIDMinterForm');"/>
                    </td>
                </tr>
            </table>


        </form>
    </div>
</div>



<%@ include file="../footer.jsp" %>
