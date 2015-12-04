//Use loader to grab the modules needed
var assetsDir = "/de.mhus.mod.aqua/res/_yui_3.0/assets/";
var buildDir = "/de.mhus.mod.aqua/res/_yui_3.0/";
var yuiConfig = { filter: 'raw' };

YUI(yuiConfig).use('dd', 'anim', 'cookie', 'json', "yui", "tabview", function(Y) {
    //Make this an Event Target so we can bubble to it
    var Portal = function() {
        Portal.superclass.constructor.apply(this, arguments);
    };
    Portal.NAME = 'portal';
    Y.extend(Portal, Y.Base);
    //This is our new bubble target..
    Y.Portal = new Portal();
 
    //Setup some private variables..
    var goingUp = false, lastY = 0, trans = {};
  
    //Simple method for stopping event propagation
    //Using this so we can detach it later
    var stopper = function(e) {
        e.stopPropagation();
    };
 
    //Handle the node:click event
    /* {{{ */
    var _nodeClick = function(e) {
        //Is the target an href?
        if (e.target.test('a')) {
            var a = e.target, anim = null, div = a.get('parentNode').get('parentNode');
    
            //Was close clicked?
            if (a.hasClass('close')) {
                //Get some Node references..
                var li = div.get('parentNode'),
                    id = li.get('id'),
                    dd = Y.DD.DDM.getDrag('#' + id),
                    data = dd.get('data'),
                    item = Y.one('#' + data.id);
 
                //Destroy the DD instance.
                dd.destroy();
                //Setup the animation for making it disappear
                anim = new Y.Anim({
                    node: div,
                    to: {
                        opacity: 0
                    },
                    duration: '.25',
                    easing: Y.Easing.easeOut
                });
                anim.on('end', function() {
                    //On end of the first anim, setup another to make it collapse
                    var anim = new Y.Anim({
                        node: div,
                        to: {
                            height: 0
                        },
                        duration: '.25',
                        easing: Y.Easing.easeOut
                    });
                    anim.on('end', function() {
                        //Remove it from the document
                        li.get('parentNode').removeChild(li);
                        item.removeClass('disabled');
                        //Setup a drag instance on the feed list
//XXX                        _setCookies();
 
                    });
                    //Run the animation
                    anim.run();
                });
                //Run the animation
                anim.run();
            }
            //Stop the click
            e.halt();
        }
    };
    /* }}} */ 
   
    //This does the calculations for when and where to move a module
    var _moveMod = function(drag, drop) {
        if (drag.get('node').hasClass('item')) {
            var dragNode = drag.get('node'),
                dropNode = drop.get('node'),
                append = false,
                padding = 30,
                xy = drag.mouseXY,
                region = drop.region,
                middle1 = region.top + ((region.bottom - region.top) / 2),
                middle2 = region.left + ((region.right - region.left) / 2),
                dir = false,
                dir1 = false,
                dir2 = false;
 
                //We could do something a little more fancy here, but we won't ;)
                if ((xy[1] < (region.top + padding))) {
                    dir1 = 'top';
                }
                if ((region.bottom - padding) < xy[1]) {
                    dir1 = 'bottom';
                }
                if ((region.right - padding) < xy[0]) {
                    dir2 = 'right';
                }
                if ((xy[0] < (region.left + padding))) {
                    dir2 = 'left';
                }
                dir = dir2;
                if (dir2 === false) {
                    dir = dir1;
                }
                switch (dir) {
                    case 'top':
                        var next = dropNode.get('nextSibling');
                        if (next) {
                            dropNode = next;
                        } else {
                            append = true;
                        }
                        break;
                    case 'bottom':
                        break;
                    case 'right':
                    case 'left':
                        break;
                }
 
 
            if ((dropNode !== null) && dir) {
                if (dropNode && dropNode.get('parentNode')) {
                    if (!append) {
                        dropNode.get('parentNode').insertBefore(dragNode, dropNode);
                    } else {
                        dropNode.get('parentNode').appendChild(dragNode);
                    }
                }
            }
            //Resync all the targets because something moved..
            Y.Lang.later(50, Y, function() {
                Y.DD.DDM.syncActiveShims(true);
            });
        }
    };
 
    /*
    Handle the drop:enter event
    Now when we get a drop enter event, we check to see if the target is an LI, then we know it's out module.
    Here is where we move the module around in the DOM.    
    */
    Y.Portal.on('drop:enter', function(e) {
        if (!e.drag || !e.drop || (e.drop !== e.target)) {
            return false;
        }
        if (e.drop.get('node').get('tagName').toLowerCase() === 'li') {
            if (e.drop.get('node').hasClass('item')) {
                _moveMod(e.drag, e.drop);
            }
        }
    });
 
    //Handle the drag:drag event
    //On drag we need to know if they are moved up or down so we can place the module in the proper DOM location.
    Y.Portal.on('drag:drag', function(e) {
        var y = e.target.mouseXY[1];
        if (y < lastY) {
            goingUp = true;
        } else {
            goingUp = false;
        }
        lastY = y;
    });
 
    /*
    Handle the drop:hit event
    Now that we have a drop on the target, we check to see if the drop was not on a LI.
    This means they dropped on the empty space of the UL.
    */
    Y.Portal.on('drag:drophit', function(e) {
        var drop = e.drop.get('node'),
            drag = e.drag.get('node');
 
        if (drop.get('tagName').toLowerCase() !== 'li') {
            if (!drop.contains(drag)) {
                drop.appendChild(drag);
            }
        }
    });
 
    //Handle the drag:start event
    //Use some CSS here to make our dragging better looking.
    Y.Portal.on('drag:start', function(e) {
        var drag = e.target;
        if (drag.target) {
            drag.target.set('locked', true);
        }
        drag.get('dragNode').set('innerHTML', drag.get('node').get('innerHTML'));
        drag.get('dragNode').setStyle('opacity','.5');
        drag.get('node').one('div.mod').setStyle('visibility', 'hidden');
        drag.get('node').addClass('moving');
    });
 
    //Handle the drag:end event
    //Replace some of the styles we changed on start drag.
    Y.Portal.on('drag:end', function(e) {
        var drag = e.target;
        if (drag.target) {
            drag.target.set('locked', false);
        }
        drag.get('node').setStyle('visibility', '');
        drag.get('node').setStyle('position', '');
        drag.get('node').one('div.mod').setStyle('visibility', '');
        drag.get('node').removeClass('moving');
        drag.get('dragNode').set('innerHTML', '');
//XXX        _setCookies(); 
    });
 
 
    //Handle going over a UL, for empty lists
    Y.Portal.on('drop:over', function(e) {
        var drop = e.drop.get('node'),
            drag = e.drag.get('node');
 
        if (drop.get('tagName').toLowerCase() !== 'li') {
            if (!drop.contains(drag)) {
                drop.appendChild(drag);
                Y.Lang.later(50, Y, function() {
                    Y.DD.DDM.syncActiveShims(true);
                });
            }
        }
    });
 
    //Create simple targets for the main lists..
    var uls = Y.all('#play ul.list');
    uls.each(function(v, k) {
        var tar = new Y.DD.Drop({
            node: v,
            padding: '20 0',
            bubbles: Y.Portal
        });
    });


    // create existing

    var needDD = Y.all("#play li.item");
    needDD.each(function(v, k) {
    	var x = v.one("h2");
    	var dd = new Y.DD.Drag({
            node: v,
            target: true,
            bubbles: Y.Portal
        }).plug(Y.Plugin.DDProxy, {
            moveOnEnd: true,
            borderStyle: 'none'
        });
    	
//    	var tar = new Y.DD.Drop({
//            node: v,
//            padding: '20 0',
//            bubbles: Y.Portal
//        });
    });

// config tab

});

