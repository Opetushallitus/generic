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
    var els = document.getElementsByClassName('nav-item');
    var elsArray = Array.prototype.slice.call(els, 0);
    elsArray.forEach(function(el) {
        // Do stuff with the element
        console.log(el.tagName);
        el.onmouseover = function(){this.children[1].style.display='block';};
        el.onmouseout = function(){this.children[1].style.display='none';};
    });
    /*
    for (var navItem in navItems) {
//        alert('xxxx');
        //navItem.onmouseover = this.children[1].style.display='block';
        console.log('tagName: '+navItem.innerHTML);
        navItem.onmouseover = function(){alert('xxx2')};
        navItem.onmouseout = function(){alert('xxx')};
    }
    */
}