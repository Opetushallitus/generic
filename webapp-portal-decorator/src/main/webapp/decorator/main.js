/*
AUI().ready(function (d) {
    var c = d.all(".nav-item");
    c.on("mouseover", a);
    c.on("mouseout", b);
    function a(g) {
        var f = g.currentTarget.one(".sub-navigation");
        f.setStyle("display", "block")
    }
    function b(g) {
        var f = g.currentTarget.one(".sub-navigation");
        f.setStyle("display", "none")
    }
});
Liferay.Portlet.ready(function (a, b) {
});
Liferay.on("allPortletsReady", function () {
});
*/


// js frame independect way of attaching javascript to body.load, idea from http://stackoverflow.com/questions/807878/javascript-that-executes-after-page-load
if(window.attachEvent) {
    window.attachEvent('onload', doOnLoad);
} else {
    if(window.onload) {
        var curronload = window.onload;
        var newonload = function() {
            curronload();
            doOnLoad();
        };
        window.onload = newonload;
    } else {
        window.onload = doOnLoad;
    }
}

function doOnLoad() {

    // apply hover opening behaviour to the navi

    var els = document.getElementsByClassName('nav-item');
    var elsArray = Array.prototype.slice.call(els, 0);
    elsArray.forEach(function(el) {
        //console.log(el.tagName);
        el.onmouseover = function(){this.children[1].style.display='block';};
        el.onmouseout = function(){this.children[1].style.display='none';};
    });

    // hide navi elems based on role

    var els = document.getElementById("main-navigation").getElementsByTagName("li");
    var elsArray = Array.prototype.slice.call(els, 0);
    var userRoles = document.getElementById("oph_user_roles").getAttribute("value");
    elsArray.forEach(function(el) {
        var requiresRole = el.getAttribute('requires-role');
        console.log("userRoles: "+userRoles+", elem: "+el.nodeValue+", requiresRole: "+requiresRole+", index: "+userRoles.indexOf(requiresRole));
        if (requiresRole != null && userRoles.indexOf(requiresRole) == -1) { // if requires some role, but user doesn't have it -> hide
            el.style.display='none';
        }
    });

}