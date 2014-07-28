    <div class="clearfooter"></div>
</div> <!—-End Container—>

<div id="footer">
    <div>
    This material is based upon work supported by the National Science Foundation under Grant Number DBI-0956426.
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