    <div class="clearfooter"></div>
</div> <!—-End Container—>

<div id="footer">
    <div>
    <a href="https://code.google.com/p/bcid/">Biocode Commons Identifiers</a> are constructed by the NSF funded
        <a href="http://biscicol.blogspot.com/">BiSciCol project</a>, powered by
        <a href="http://ezid.cdlib.org/">EZID</a>, and fall under the
        <a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>.
    </div>
</div>

<script>
    $(document).ready(function() {
        $(".pwcheck").pwstrength({texts:['weak', 'good', 'good', 'strong', 'strong'],
                                  classes:['pw-weak', 'pw-good', 'pw-good', 'pw-strong', 'pw-strong']});
    });
    $(document).ajaxStop(function() {
        $(".pwcheck").pwstrength({texts:['weak', 'good', 'good', 'strong', 'strong'],
                                  classes:['pw-weak', 'pw-good', 'pw-good', 'pw-strong', 'pw-strong']});
    });
</script>
</body>
</html>